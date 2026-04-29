import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Grid,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';

export default function AuthPage({ authForm, authMode, error, onChangeForm, onChangeMode, onLogin, onRegister }) {
  const isLogin = authMode === 'login';

  return (
    <Container maxWidth="lg" sx={{ py: 6 }}>
      <Paper className="hero" sx={{ p: { xs: 3, md: 5 }, mb: 3 }}>
        <Typography variant="h1" sx={{ fontSize: { xs: 42, md: 72 } }}>
          Prime<span className="accent">Wheel</span>
        </Typography>
        <Typography variant="h5" color="text.secondary" sx={{ maxWidth: 720 }}>
          Войдите как администратор для управления системой или зарегистрируйтесь как клиент для бронирования авто.
        </Typography>
      </Paper>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card className="muted-card">
            <CardContent>
              <Tabs value={authMode} onChange={(_, value) => onChangeMode(value)} sx={{ mb: 2 }}>
                <Tab value="login" label="Вход" />
                <Tab value="register" label="Регистрация" />
              </Tabs>

              {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

              <Box component="form" onSubmit={isLogin ? onLogin : onRegister}>
                <Stack spacing={2}>
                  {!isLogin && (
                    <>
                      <TextField label="Имя" required value={authForm.name}
                        onChange={(event) => onChangeForm({ ...authForm, name: event.target.value })} />
                      <TextField label="Телефон" value={authForm.phone}
                        onChange={(event) => onChangeForm({ ...authForm, phone: event.target.value })} />
                      <TextField label="Водительское удостоверение" required value={authForm.driverLicense}
                        onChange={(event) => onChangeForm({ ...authForm, driverLicense: event.target.value })} />
                    </>
                  )}
                  <TextField label="Email" required type="email" value={authForm.email}
                    onChange={(event) => onChangeForm({ ...authForm, email: event.target.value })} />
                  <TextField label="Пароль" required type="password" value={authForm.password}
                    onChange={(event) => onChangeForm({ ...authForm, password: event.target.value })} />
                  <Button type="submit" variant="contained">
                    {isLogin ? 'Войти' : 'Создать аккаунт'}
                  </Button>
                </Stack>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 7 }}>
          <Card className="muted-card">
            <CardContent>
              <Typography variant="h5" gutterBottom>Добро пожаловать в PrimeWheel</Typography>
              <Stack spacing={2}>
                <Box>
                  <Typography fontWeight={800}>Управляйте поездками удобно</Typography>
                  <Typography color="text.secondary">
                    Войдите в аккаунт, чтобы продолжить: просматривать автомобили, оформлять бронирования
                    и отслеживать их статус в одном месте.
                  </Typography>
                </Box>
                <Box>
                  <Typography fontWeight={800}>Еще нет аккаунта?</Typography>
                  <Typography color="text.secondary">
                    Зарегистрируйтесь за минуту и начните бронирование. После входа будут доступны
                    персональные данные и история ваших поездок.
                  </Typography>
                </Box>
                <Alert severity="info">
                  Чтобы начать работу, войдите или зарегистрируйтесь.
                </Alert>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
}
