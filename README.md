# YAM Fondos

Backend para suscribirse y cancelar fondos, ver el historial y notificar por email o SMS.

## Run

JDK 21. Mongo con Docker:

```
docker compose up -d mongodb
.\gradlew.bat bootRun
```

http://localhost:8080  
http://localhost:8080/swagger-ui.html

Al subir queda el cliente `client-001` con 500.000 COP y los 5 fondos.

## API

```
GET    /api/v1/funds
GET    /api/v1/clients/{clientId}
POST   /api/v1/clients/{clientId}/subscriptions
DELETE /api/v1/clients/{clientId}/subscriptions/{fundId}
GET    /api/v1/clients/{clientId}/transactions
```

```
POST /api/v1/clients/client-001/subscriptions
{ "fundId": "1", "amount": 75000 }
```

Si no mandas `amount`, toma el mínimo del fondo. Sin saldo:

`No tiene saldo disponible para vincularse al fondo <Nombre del fondo>`

Colección de requests en `http/api.http`.

## Notas

- No se puede tener el mismo fondo dos veces.
- Cancelar devuelve el valor vinculado.
- Si SNS/email falla, la suscripción no se deshace.
- El cliente tiene `version` para no pisar el saldo si llegan dos requests juntos.

Despliegue: `deployment/cloudformation.yaml`  
SQL: `sql/parte2-clientes-producto-sucursales.sql`  
Tests: `.\gradlew.bat test`
