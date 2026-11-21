# Cocina UPeU

Tienda de comida peruana con Angular 18, Spring Boot, Oracle y Mercado Pago Checkout Pro. Incluye carta, carrito, cuentas de clientes, historial de pedidos y gestión del catálogo por administradores.

## Ejecutar en local

Necesitas Java 21 y Node.js 22 LTS. Abre dos terminales desde la raíz.

```bash
cd lp2_gp2
bash mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

```bash
cd frontend-gp2
npm ci
npm start
```

Abre [Cocina UPeU](http://localhost:4200). El perfil `demo` usa H2 en memoria y carga tres platos. Puedes crear una cuenta y probar la carta y el carrito. El pago se habilita cuando configuras Mercado Pago. El servidor Angular pasa las rutas `/lp2_gp2/**` al backend local en el puerto 8082.

## Configuración de Mercado Pago

1. Crea una aplicación de **Checkout Pro (preferencias)** en Mercado Pago Perú. Empieza con las credenciales de prueba y la cuenta de comprador de prueba.
2. Publica la aplicación en un origen HTTPS accesible desde Internet. Sirve Angular en `/`, con fallback a `index.html` para rutas como `/pedido/8`, y dirige `/lp2_gp2/**` al backend sin quitar el prefijo. Esta ruta incluye tanto la API como las imágenes.
3. Configura **Webhooks → Pagos** para `https://tu-dominio.pe/lp2_gp2/api/v1/pagos/mercadopago/webhook` y guarda la firma secreta en el backend. Configura el ambiente correspondiente a las credenciales.

| Variable | Valor |
| --- | --- |
| `APP_PUBLIC_URL` | Origen HTTPS de la tienda, por ejemplo `https://tu-dominio.pe`. |
| `MERCADO_PAGO_ACCESS_TOKEN` | Access Token de la aplicación, guardado como secreto del servidor. |
| `MERCADO_PAGO_WEBHOOK_SECRET` | Clave secreta de Webhooks de la aplicación, guardada en el servidor. |
| `MERCADO_PAGO_COLLECTOR_ID` | ID numérico de la cuenta vendedora asociada al Access Token. Puedes comprobarlo en `GET https://api.mercadopago.com/users/me` autenticado con ese token. |

Valida pagos aprobados, pendientes y rechazados con las credenciales de prueba y comprueba que «Mis pedidos» refleje la notificación. Usa credenciales y configuración de producción solo al habilitar cobros reales. Consulta las guías oficiales de [compras de prueba](https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-preferences/integration-test/test-purchases) y [notificaciones](https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-preferences/payment-notifications).

El backend fija el importe en soles a partir del catálogo y reserva el stock por 25 minutos. Da dos minutos adicionales para recibir la confirmación antes de liberar la reserva. Si un pago se aprueba tras liberar el stock, o aparece un cobro adicional para el mismo pedido, solicita el reembolso con una clave idempotente. Los medios de pago en efectivo diferido quedan excluidos por la duración de la reserva. La pantalla de retorno consulta al backend. La confirmación llega de Mercado Pago mediante Webhook firmado.

## Base de datos y sesiones

- Para usar Oracle, ejecuta el backend sin el perfil `demo` y define `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. `DB_DDL_AUTO` vale `update` por defecto. Valida el cambio de esquema en una copia de tu base antes de aplicarlo en producción. `STORAGE_LOCATION` puede apuntar a un volumen persistente para las imágenes.
- La sesión de servidor expira tras 30 minutos de inactividad. El perfil principal usa cookies seguras y debe servirse por HTTPS. Para un entorno de desarrollo HTTP con Oracle, puedes establecer `SESSION_COOKIE_SECURE=false`.
- El límite de inicio de sesión es de 8 intentos por cuenta y 40 por IP cada 15 minutos. Tras un proxy inverso, configura Tomcat para confiar solo en el proxy propio y haz que este escriba la IP de origen a partir de la conexión, para que el límite por IP diferencie a los visitantes. Sesiones y contadores residen en la instancia de Spring.
- Las cuentas nuevas reciben el rol `CLIENTE`. Los administradores existentes siguen usando el rol `ADMIN` guardado en la base. Los pedidos anteriores conservan su estado histórico y se identifican por carecer de una reserva de pago.

## Verificar cambios

```bash
(cd lp2_gp2 && bash mvnw verify)
(cd frontend-gp2 && npm run build)
```

Las pruebas del backend usan H2 y respuestas simuladas del proveedor. Cubren registro, sesión, cierre de sesión, límites de intentos, aislamiento de pedidos, firma del webhook, confirmación de importes, duplicados y vencimientos.
