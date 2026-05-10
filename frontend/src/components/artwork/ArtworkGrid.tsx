import React from 'react';
import { Grid } from '@mui/material';
import ArtworkCard from './ArtworkCard';
import { Artwork } from '../../types';

interface ArtworkGridProps {
  artworks: Artwork[];
  onFavoriteToggle?: () => void;
}

const ArtworkGrid: React.FC<ArtworkGridProps> = ({ artworks, onFavoriteToggle }) => {
  return (
    <Grid container spacing={3}>
      {artworks.map((artwork) => (
        <Grid size={{ xs: 12, sm: 6, md: 4 }} key={artwork.id}>
          <ArtworkCard artwork={artwork} onFavoriteToggle={onFavoriteToggle} />
        </Grid>
      ))}
    </Grid>
  );
};

export default ArtworkGrid;
