import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Stack,
  Tab,
  Tabs,
  TextField,
} from '@mui/material';

export default function AuthPage({ authForm, authMode, error, onChangeForm, onChangeMode, onLogin, onRegister }) {
  const isLogin = authMode === 'login';

  return (
    <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', py: 4 }}>
      <Card className="muted-card" sx={{ width: '100%', backdropFilter: 'blur(6px)' }}>
        <CardContent sx={{ p: { xs: 2.5, md: 3.5 } }}>
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
              <TextField placeholder="Email" required type="email" value={authForm.email}
                onChange={(event) => onChangeForm({ ...authForm, email: event.target.value })} />
              <TextField placeholder="Пароль" required type="password" value={authForm.password}
                onChange={(event) => onChangeForm({ ...authForm, password: event.target.value })} />
              <Button type="submit" variant="contained" sx={{ mt: 1 }}>
                {isLogin ? 'Войти' : 'Создать аккаунт'}
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Container>
  );
}
