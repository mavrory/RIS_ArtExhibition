package com.digitalart.admin.application;

import com.digitalart.admin.application.dto.*;
import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.artwork.domain.Artwork;
import com.digitalart.artwork.infrastructure.ArtworkRepository;
import com.digitalart.artwork.infrastructure.CommentRepository;
import com.digitalart.artwork.infrastructure.FavoriteRepository;
import com.digitalart.exhibition.infrastructure.ExhibitionRepository;
import com.digitalart.order.domain.Order;
import com.digitalart.order.domain.OrderStatus;
import com.digitalart.order.infrastructure.OrderRepository;
import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.subscription.infrastructure.SubscriptionRepository;
import com.digitalart.user.application.dto.UserDto;
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ArtworkRepository artworkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final OrderRepository orderRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PlatformStatisticsDto getPlatformStatistics() {
        long totalUsers = userRepository.count();
        long totalArtists = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ARTIST")))
                .count();
        long totalArtworks = artworkRepository.count();
        long totalExhibitions = exhibitionRepository.count();
        
        double totalRevenue = userRepository.findAll().stream()
                .mapToDouble(User::getBalance)
                .sum();

        return PlatformStatisticsDto.builder()
                .totalUsers((int) totalUsers)
                .totalArtists((int) totalArtists)
                .totalArtworks((int) totalArtworks)
                .totalExhibitions((int) totalExhibitions)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Transactional
    public UserDto updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updates.containsKey("balance")) {
            user.setBalance(((Number) updates.get("balance")).doubleValue());
        }

        userRepository.save(user);
        return convertToDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Don't allow deleting admin users
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            throw new RuntimeException("Cannot delete admin users");
        }
        
        userRepository.delete(user);
    }

    @Transactional
    public UserDto toggleUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
        } else {
            user.getRoles().add(role);
        }

        userRepository.save(user);
        return convertToDto(user);
    }

    public List<ArtworkDto> getAllArtworks() {
        System.out.println("=== Admin: Getting all artworks ===");
        List<Artwork> artworks = artworkRepository.findAll();
        System.out.println("Found " + artworks.size() + " artworks");
        
        return artworks.stream()
                .map(artwork -> {
                    try {
                        System.out.println("Processing artwork: " + artwork.getId() + " - " + artwork.getTitle());
                        System.out.println("Author ID: " + artwork.getAuthorId());
                        ArtworkDto dto = convertArtworkToDto(artwork);
                        System.out.println("DTO authorName: " + dto.getAuthorName());
                        return dto;
                    } catch (Exception e) {
                        System.err.println("ERROR processing artwork " + artwork.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        // Return a DTO with "Unknown Artist" as fallback
                        return ArtworkDto.builder()
                                .id(artwork.getId())
                                .title(artwork.getTitle())
                                .description(artwork.getDescription())
                                .price(artwork.getPrice())
                                .authorId(artwork.getAuthorId())
                                .authorName("Unknown Artist")
                                .previewUrl(artwork.getPreviewUrl())
                                .isSold(artwork.getIsSold())
                                .viewsCount(artwork.getViewsCount())
                                .createdAt(artwork.getCreatedAt())
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteArtwork(Long artworkId) {
        System.out.println("=== Attempting to delete artwork: " + artworkId + " ===");
        
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork not found"));
        
        System.out.println("Artwork found: " + artwork.getTitle());
        
        try {
            // Delete related records first
            // Delete comments
            System.out.println("Deleting comments...");
            List<com.digitalart.artwork.domain.Comment> comments = commentRepository.findByArtworkIdOrderByCreatedAtDesc(artworkId);
            System.out.println("Found " + comments.size() + " comments to delete");
            commentRepository.deleteAll(comments);
            
            // Delete favorites
            System.out.println("Deleting favorites...");
            List<com.digitalart.artwork.domain.Favorite> favorites = favoriteRepository.findAll().stream()
                    .filter(f -> f.getId().getArtworkId().equals(artworkId))
                    .collect(Collectors.toList());
            System.out.println("Found " + favorites.size() + " favorites to delete");
            favoriteRepository.deleteAll(favorites);
            
            // Check if there are any orders
            System.out.println("Checking for orders...");
            List<Order> orders = orderRepository.findAll().stream()
                    .filter(o -> o.getArtworkId().equals(artworkId))
                    .collect(Collectors.toList());
            System.out.println("Found " + orders.size() + " orders");
            
            if (!orders.isEmpty()) {
                System.out.println("WARNING: Artwork has orders. Cannot delete.");
                throw new BusinessException("Cannot delete artwork with existing orders. Found " + orders.size() + " order(s).");
            }
            
            // Delete the artwork
            System.out.println("Deleting artwork...");
            artworkRepository.delete(artwork);
            System.out.println("=== Artwork deleted successfully ===");
            
        } catch (Exception e) {
            System.err.println("=== ERROR deleting artwork ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public DetailedStatisticsDto getDetailedStatistics() {
        // Basic counts
        long totalUsers = userRepository.count();
        long totalArtists = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ARTIST")))
                .count();
        long totalArtworks = artworkRepository.count();
        long totalExhibitions = exhibitionRepository.count();
        
        // Orders statistics
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();
        long cancelledOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        
        // Revenue statistics
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSales = BigDecimal.valueOf(completedOrders);
        
        List<Artwork> allArtworks = artworkRepository.findAll();
        BigDecimal averageArtworkPrice = allArtworks.isEmpty() ? BigDecimal.ZERO :
                allArtworks.stream()
                        .map(Artwork::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allArtworks.size()), 2, RoundingMode.HALF_UP);
        
        // Engagement statistics
        long totalViews = allArtworks.stream()
                .mapToLong(Artwork::getViewsCount)
                .sum();
        long totalFavorites = favoriteRepository.count();
        long totalComments = commentRepository.count();
        long totalSubscriptions = subscriptionRepository.count();
        
        // Monthly statistics
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        long newArtworksThisMonth = allArtworks.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        long salesThisMonth = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .filter(o -> o.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        // Top artists
        List<TopArtistDto> topArtists = calculateTopArtists();
        
        // Top artworks
        List<TopArtworkDto> topArtworks = calculateTopArtworks();
        
        // Users by role
        Map<String, Integer> usersByRole = new HashMap<>();
        usersByRole.put("USER", (int) userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("USER")))
                .count());
        usersByRole.put("ARTIST", (int) totalArtists);
        usersByRole.put("ADMIN", (int) userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")))
                .count());
        
        // Revenue by month (last 6 months)
        Map<String, BigDecimal> revenueByMonth = calculateRevenueByMonth(allOrders);
        
        return DetailedStatisticsDto.builder()
                .totalUsers((int) totalUsers)
                .totalArtists((int) totalArtists)
                .totalArtworks((int) totalArtworks)
                .totalExhibitions((int) totalExhibitions)
                .totalOrders((int) totalOrders)
                .completedOrders((int) completedOrders)
                .pendingOrders((int) pendingOrders)
                .cancelledOrders((int) cancelledOrders)
                .totalRevenue(totalRevenue)
                .totalSales(totalSales)
                .averageArtworkPrice(averageArtworkPrice)
                .totalViews(totalViews)
                .totalFavorites(totalFavorites)
                .totalComments(totalComments)
                .totalSubscriptions(totalSubscriptions)
                .newUsersThisMonth((int) newUsersThisMonth)
                .newArtworksThisMonth((int) newArtworksThisMonth)
                .salesThisMonth((int) salesThisMonth)
                .topArtists(topArtists)
                .topArtworks(topArtworks)
                .usersByRole(usersByRole)
                .revenueByMonth(revenueByMonth)
                .build();
    }
    
    private List<TopArtistDto> calculateTopArtists() {
        List<User> artists = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ARTIST")))
                .collect(Collectors.toList());
        
        return artists.stream()
                .map(artist -> {
                    List<Artwork> artistArtworks = artworkRepository.findAll().stream()
                            .filter(a -> a.getAuthorId().equals(artist.getId()))
                            .collect(Collectors.toList());
                    
                    int artworksCount = artistArtworks.size();
                    int salesCount = (int) artistArtworks.stream()
                            .filter(Artwork::getIsSold)
                            .count();
                    
                    BigDecimal totalRevenue = artistArtworks.stream()
                            .filter(Artwork::getIsSold)
                            .map(Artwork::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    long subscribersCount = subscriptionRepository.findAll().stream()
                            .filter(s -> s.getArtistId().equals(artist.getId()))
                            .count();
                    
                    return TopArtistDto.builder()
                            .artistId(artist.getId())
                            .artistName(artist.getUsername())
                            .artworksCount(artworksCount)
                            .salesCount(salesCount)
                            .totalRevenue(totalRevenue)
                            .subscribersCount(subscribersCount)
                            .build();
                })
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private List<TopArtworkDto> calculateTopArtworks() {
        return artworkRepository.findAll().stream()
                .map(artwork -> {
                    User author = userRepository.findById(artwork.getAuthorId()).orElse(null);
                    
                    long favoritesCount = favoriteRepository.findAll().stream()
                            .filter(f -> f.getId().getArtworkId().equals(artwork.getId()))
                            .count();
                    
                    return TopArtworkDto.builder()
                            .artworkId(artwork.getId())
                            .title(artwork.getTitle())
                            .artistName(author != null ? author.getUsername() : "Unknown")
                            .price(artwork.getPrice())
                            .viewsCount(artwork.getViewsCount())
                            .favoritesCount(favoritesCount)
                            .isSold(artwork.getIsSold())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getViewsCount(), a.getViewsCount()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private Map<String, BigDecimal> calculateRevenueByMonth(List<Order> orders) {
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            
            String monthKey = monthStart.getMonth().toString() + " " + monthStart.getYear();
            
            BigDecimal monthRevenue = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .filter(o -> o.getCreatedAt().isAfter(monthStart) && o.getCreatedAt().isBefore(monthEnd))
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            revenueByMonth.put(monthKey, monthRevenue);
        }
        
        return revenueByMonth;
    }

    public String generateUsersReport() {
        List<User> users = userRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Username,Email,Balance,Roles,Created At\n");
        
        for (User user : users) {
            String roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(";"));
            
            csv.append(String.format("%d,%s,%s,%.2f,%s,%s\n",
                    user.getId(),
                    escapeCsv(user.getUsername()),
                    escapeCsv(user.getEmail()),
                    user.getBalance(),
                    roles,
                    user.getCreatedAt().toString()));
        }
        
        return csv.toString();
    }

    public String generateArtworksReport() {
        List<Artwork> artworks = artworkRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Artist,Price,Views,Is Sold,Created At\n");
        
        for (Artwork artwork : artworks) {
            User author = userRepository.findById(artwork.getAuthorId()).orElse(null);
            String authorName = author != null ? author.getUsername() : "Unknown";
            
            csv.append(String.format("%d,%s,%s,%.2f,%d,%s,%s\n",
                    artwork.getId(),
                    escapeCsv(artwork.getTitle()),
                    escapeCsv(authorName),
                    artwork.getPrice(),
                    artwork.getViewsCount(),
                    artwork.getIsSold() ? "Yes" : "No",
                    artwork.getCreatedAt().toString()));
        }
        
        return csv.toString();
    }

    public String generateOrdersReport() {
        List<Order> orders = orderRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,User,Artwork,Status,Total Price,Created At\n");
        
        for (Order order : orders) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            Artwork artwork = artworkRepository.findById(order.getArtworkId()).orElse(null);
            
            String username = user != null ? user.getUsername() : "Unknown";
            String artworkTitle = artwork != null ? artwork.getTitle() : "Unknown";
            
            csv.append(String.format("%d,%s,%s,%s,%.2f,%s\n",
                    order.getId(),
                    escapeCsv(username),
                    escapeCsv(artworkTitle),
                    order.getStatus().toString(),
                    order.getTotalPrice(),
                    order.getCreatedAt().toString()));
        }
        
        return csv.toString();
    }

    public String generateRevenueReport() {
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        StringBuilder csv = new StringBuilder();
        csv.append("Month,Revenue,Orders Count\n");
        
        Map<String, BigDecimal> revenueByMonth = calculateRevenueByMonth(orderRepository.findAll());
        
        for (Map.Entry<String, BigDecimal> entry : revenueByMonth.entrySet()) {
            String month = entry.getKey();
            BigDecimal revenue = entry.getValue();
            
            long ordersCount = completedOrders.stream()
                    .filter(o -> {
                        String orderMonth = o.getCreatedAt().getMonth().toString() + " " + o.getCreatedAt().getYear();
                        return orderMonth.equals(month);
                    })
                    .count();
            
            csv.append(String.format("%s,%.2f,%d\n", month, revenue, ordersCount));
        }
        
        return csv.toString();
    }

    public String generateArtistsReport() {
        List<TopArtistDto> topArtists = calculateTopArtists();
        StringBuilder csv = new StringBuilder();
        csv.append("Artist ID,Artist Name,Artworks Count,Sales Count,Total Revenue,Subscribers Count\n");
        
        for (TopArtistDto artist : topArtists) {
            csv.append(String.format("%d,%s,%d,%d,%.2f,%d\n",
                    artist.getArtistId(),
                    escapeCsv(artist.getArtistName()),
                    artist.getArtworksCount(),
                    artist.getSalesCount(),
                    artist.getTotalRevenue(),
                    artist.getSubscribersCount()));
        }
        
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .balance(user.getBalance())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    private ArtworkDto convertArtworkToDto(Artwork artwork) {
        System.out.println("Converting artwork to DTO: " + artwork.getId());
        System.out.println("Looking for author with ID: " + artwork.getAuthorId());
        
        User author = userRepository.findById(artwork.getAuthorId()).orElse(null);
        
        String authorName = "Unknown";
        if (author != null) {
            authorName = author.getUsername();
            System.out.println("Author found: " + authorName);
        } else {
            System.err.println("WARNING: Author not found for artwork " + artwork.getId());
        }
        
        return ArtworkDto.builder()
                .id(artwork.getId())
                .title(artwork.getTitle())
                .description(artwork.getDescription())
                .price(artwork.getPrice())
                .authorId(artwork.getAuthorId())
                .authorName(authorName)
                .previewUrl(artwork.getPreviewUrl())
                .isSold(artwork.getIsSold())
                .viewsCount(artwork.getViewsCount())
                .createdAt(artwork.getCreatedAt())
                .build();
    }
}
