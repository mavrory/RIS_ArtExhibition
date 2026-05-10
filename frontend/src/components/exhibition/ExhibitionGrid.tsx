import React from 'react';
import Grid from '@mui/material/Grid';
import ExhibitionCard from './ExhibitionCard';
import { Exhibition } from '../../types';

interface ExhibitionGridProps {
  exhibitions: Exhibition[];
}

const ExhibitionGrid: React.FC<ExhibitionGridProps> = ({ exhibitions }) => {
  return (
    <Grid container spacing={3}>
      {exhibitions.map((exhibition) => (
        <Grid size={{ xs: 12, sm: 6, md: 4 }} key={exhibition.id}>
          <ExhibitionCard exhibition={exhibition} />
        </Grid>
      ))}
    </Grid>
  );
};

export default ExhibitionGrid;
