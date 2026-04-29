import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import AlbumIcon from '@mui/icons-material/Album';

function displayCar(car) {
  return [car.brand, car.model, car.licensePlate].filter(Boolean).join(' ');
}

export function CarCards({ cars, onSelectCar }) {
  return (
    <Grid container spacing={3}>
      {cars.map((car) => (
        <Grid key={car.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
          <Card className="car-card" onClick={() => onSelectCar(car)}>
            <CarImage car={car} height={180} />
            <CardContent>
              <Typography variant="h6" fontWeight={900}>{car.brand} {car.model}</Typography>
              <Typography color="text.secondary" sx={{ mb: 1 }}>
                {car.locationCity ? `${car.locationCity}, ${car.locationAddress}` : 'Локация уточняется'}
              </Typography>
              <Stack className="feature-chip-row" direction="row" spacing={1.5} useFlexGap flexWrap="wrap" sx={{ mb: 2 }}>
                {(car.features || []).slice(0, 3).map((feature) => (
                  <Chip className="feature-chip" key={feature} size="small" label={feature} />
                ))}
              </Stack>
              <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">от</Typography>
                  <Typography fontWeight={900}>{car.pricePerMinute} BYN/мин</Typography>
                </Box>
                <Chip color={car.available ? 'primary' : 'default'} label={car.available ? 'Свободна' : 'Занята'} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}

export function CarDetails({ car, onBack, onBookCar }) {
  return (
    <Paper sx={{ p: { xs: 2, md: 4 } }}>
      <Button onClick={onBack} sx={{ mb: 2 }}>Вернуться в автопарк</Button>
      <Grid container spacing={4}>
        <Grid size={{ xs: 12, md: 7 }}>
          <CarImage car={car} height={420} />
        </Grid>
        <Grid size={{ xs: 12, md: 5 }}>
          <Typography variant="h3" fontWeight={900} gutterBottom>
            {car.brand} {car.model}
          </Typography>
          <Stack direction="row" spacing={1.5} useFlexGap flexWrap="wrap" sx={{ mb: 3 }}>
            <Chip label={`${car.year || 'Год не указан'}`} />
            <Chip label={`Топливо ${car.fuelLevel ?? 0}%`} />
            <Chip label={car.licensePlate} />
            <Chip color={car.available ? 'primary' : 'default'} label={car.available ? 'Свободна сейчас' : 'Занята'} />
          </Stack>
          <Typography color="text.secondary" paragraph>
            {displayCar(car)} доступен для аренды в сервисе PrimeWheel. Выберите этот автомобиль в разделе
            бронирований, укажите дату и длительность аренды.
          </Typography>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h5" fontWeight={900}>Стоимость аренды</Typography>
          <Typography variant="h4" color="primary" fontWeight={900} sx={{ mb: 2 }}>
            {car.pricePerMinute} BYN/мин
          </Typography>
          <Typography fontWeight={800}>Локация</Typography>
          <Typography color="text.secondary" sx={{ mb: 2 }}>
            {car.locationCity ? `${car.locationCity}, ${car.locationAddress}` : 'Локация будет назначена администратором'}
          </Typography>
          <Typography fontWeight={800}>Особенности</Typography>
          <Stack direction="row" spacing={1.5} useFlexGap flexWrap="wrap" sx={{ mt: 1 }}>
            {(car.features || []).length > 0
              ? car.features.map((feature) => <Chip key={feature} label={feature} />)
              : <Typography color="text.secondary">Особенности пока не указаны</Typography>}
          </Stack>
          <Button
            fullWidth
            variant="contained"
            disabled={!car.available}
            sx={{ mt: 3 }}
            onClick={() => onBookCar(car)}
          >
            Забронировать этот автомобиль
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
}

function CarImage({ car, height }) {
  return (
    <Box
      sx={{
        height,
        borderRadius: 2,
        bgcolor: 'rgba(255,255,255,0.04)',
        backgroundImage: car.imageUrl ? `url("${car.imageUrl}")` : 'none',
        backgroundPosition: 'center',
        backgroundSize: 'cover',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        border: '1px solid rgba(255,255,255,0.08)',
      }}
    >
      {!car.imageUrl && (
        <Stack alignItems="center" spacing={1}>
          <AlbumIcon color="primary" sx={{ fontSize: 72 }} />
          <Typography color="text.secondary">Фото добавит администратор</Typography>
        </Stack>
      )}
    </Box>
  );
}
