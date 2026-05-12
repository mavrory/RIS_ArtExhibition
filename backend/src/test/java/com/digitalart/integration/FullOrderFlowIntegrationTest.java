package com.digitalart.integration;

import com.digitalart.artwork.application.ArtworkService;
import com.digitalart.artwork.application.dto.ArtworkDetailDto;
import com.digitalart.artwork.application.dto.ArtworkDto;
import com.digitalart.artwork.application.dto.CreateArtworkRequest;
import com.digitalart.order.application.OrderService;
import com.digitalart.order.application.dto.CreateOrderRequest;
import com.digitalart.order.application.dto.OrderDto;
import com.digitalart.payment.application.PaymentService;
import com.digitalart.payment.application.dto.PaymentDto;
import com.digitalart.payment.application.dto.ProcessPaymentRequest;
import com.digitalart.user.application.AuthService;
import com.digitalart.user.application.dto.AuthResponse;
import com.digitalart.user.application.dto.RegisterRequest;
import com.digitalart.wallet.application.UserWalletService;
import com.digitalart.wallet.application.dto.DepositRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testcontainers")
@Disabled("Requires Docker running with Testcontainers")
class FullOrderFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ArtworkService artworkService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserWalletService walletService;

    private String artistEmail;
    private String buyerEmail;
    private Long buyerId;

    @BeforeEach
    void setUp() {
        artistEmail = "artist_" + System.nanoTime() + "@test.com";
        buyerEmail = "buyer_" + System.nanoTime() + "@test.com";

        RegisterRequest artistReg = new RegisterRequest();
        artistReg.setEmail(artistEmail);
        artistReg.setPassword("password123");
        artistReg.setUsername("testartist_" + System.nanoTime());
        artistReg.setIsArtist(true);
        authService.register(artistReg);

        RegisterRequest buyerReg = new RegisterRequest();
        buyerReg.setEmail(buyerEmail);
        buyerReg.setPassword("password123");
        buyerReg.setUsername("testbuyer_" + System.nanoTime());
        buyerReg.setIsArtist(false);
        AuthResponse buyerResponse = authService.register(buyerReg);
        buyerId = buyerResponse.getUser().getId();

        DepositRequest deposit = new DepositRequest();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setPaymentMethod("CREDIT_CARD");
        walletService.deposit(buyerId, deposit);
    }

    private MultipartFile createDummyImage() {
        try {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 100, 100);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            return new MockMultipartFile("image", "test.jpg", "image/jpeg", baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateArtworkAsArtist() throws Exception {
        CreateArtworkRequest request = new CreateArtworkRequest();
        request.setTitle("Integration Test Artwork");
        request.setDescription("Created during integration test");
        request.setPrice(new BigDecimal("100.00"));

        ArtworkDto dto = artworkService.createArtwork(request, createDummyImage(), artistEmail);

        assertNotNull(dto.getId());
        assertEquals("Integration Test Artwork", dto.getTitle());
        assertFalse(dto.getIsSold());
    }

    @Test
    void testCreateArtworkAsRegularUser_shouldFail() throws Exception {
        CreateArtworkRequest request = new CreateArtworkRequest();
        request.setTitle("Should Fail");
        request.setDescription("Regular user cannot create");
        request.setPrice(new BigDecimal("50.00"));

        assertThrows(Exception.class, () -> {
            artworkService.createArtwork(request, createDummyImage(), buyerEmail);
        });
    }

    @Test
    void testFullPurchaseFlow() throws Exception {
        CreateArtworkRequest createReq = new CreateArtworkRequest();
        createReq.setTitle("Purchasable Artwork");
        createReq.setDescription("To be purchased");
        createReq.setPrice(new BigDecimal("100.00"));

        ArtworkDto artwork = artworkService.createArtwork(createReq, createDummyImage(), artistEmail);
        Long artId = artwork.getId();

        assertFalse(artwork.getIsSold());

        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setArtworkId(artId);
        OrderDto order = orderService.createOrder(orderReq, buyerEmail);

        assertNotNull(order);
        assertEquals("PENDING", order.getStatus());

        ProcessPaymentRequest paymentReq = new ProcessPaymentRequest();
        paymentReq.setOrderId(order.getId());
        PaymentDto payment = paymentService.processPayment(paymentReq, buyerEmail);

        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());

        ArtworkDetailDto detail = artworkService.getArtworkDetail(artId, buyerEmail);
        assertTrue(detail.getIsPurchased());

        byte[] fullImage = artworkService.getArtworkImage(artId, buyerEmail);
        assertNotNull(fullImage);

        BigDecimal buyerBalance = walletService.getBalance(buyerId);
        assertEquals(0, new BigDecimal("400.00").compareTo(buyerBalance));
    }

    @Test
    void testGetArtworkDetail_incrementsViews() throws Exception {
        CreateArtworkRequest createReq = new CreateArtworkRequest();
        createReq.setTitle("View Test Artwork");
        createReq.setDescription("Testing view count");
        createReq.setPrice(new BigDecimal("50.00"));

        ArtworkDto artwork = artworkService.createArtwork(createReq, createDummyImage(), artistEmail);
        Long artId = artwork.getId();

        artworkService.getArtworkDetail(artId, buyerEmail);
        ArtworkDetailDto detail2 = artworkService.getArtworkDetail(artId, buyerEmail);

        assertEquals(2L, detail2.getViewsCount());
    }

    @Test
    void testDepositLimit() {
        DepositRequest overLimit = new DepositRequest();
        overLimit.setAmount(new BigDecimal("10001.00"));
        overLimit.setPaymentMethod("CREDIT_CARD");

        assertThrows(Exception.class, () -> walletService.deposit(buyerId, overLimit));
    }

    @Test
    void testPreventDuplicatePayment() throws Exception {
        CreateArtworkRequest createReq = new CreateArtworkRequest();
        createReq.setTitle("Unique Artwork");
        createReq.setDescription("Testing duplicate payment prevention");
        createReq.setPrice(new BigDecimal("50.00"));

        ArtworkDto artwork = artworkService.createArtwork(createReq, createDummyImage(), artistEmail);

        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setArtworkId(artwork.getId());
        OrderDto order = orderService.createOrder(orderReq, buyerEmail);

        ProcessPaymentRequest paymentReq = new ProcessPaymentRequest();
        paymentReq.setOrderId(order.getId());
        paymentService.processPayment(paymentReq, buyerEmail);

        assertThrows(Exception.class, () -> paymentService.processPayment(paymentReq, buyerEmail));
    }
}
