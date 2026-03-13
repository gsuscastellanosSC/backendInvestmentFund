# BTG Pactual - Sistema de Gestión de Fondos de Inversión

Esta aplicación permite a los clientes gestionar sus suscripciones a fondos de inversión de manera autónoma, con un enfoque en alta disponibilidad, escalabilidad y mantenibilidad.

## 🚀 Tecnologías Utilizadas

* **Java 21**: Uso de Records para inmutabilidad y sintaxis moderna.
* **Spring Boot 3.2.x**: Framework base para la aplicación.
* **Spring WebFlux**: Programación reactiva para el manejo de flujos no bloqueantes.
* **Spring Security + JWT**: Protección de endpoints y autenticación stateless.
* **Amazon DynamoDB**: Base de datos NoSQL de baja latencia.
* **Project Reactor**: Implementación de flujos reactivos (Mono y Flux).
* **JUnit 5 & Mockito**: Pruebas unitarias de alta cobertura.

## 🏗️ Arquitectura: Hexagonal (Ports & Adapters)

Se ha implementado una **Arquitectura Hexagonal** para desacoplar la lógica de negocio de los detalles de infraestructura:

1.  **Domain**: Contiene las entidades (Records), excepciones de negocio y servicios de validación puros.
2.  **Application**: Orquesta los casos de uso (Ej: `SubscribeToFundUseCase`) interactuando solo con interfaces (Puertos).
3.  **Infrastructure**: Implementa los adaptadores para DynamoDB, seguridad JWT y los endpoints REST funcionales.

## 🛠️ Instalación y Ejecución

### Requisitos Previos
* Java 21 instalado.
* Maven 3.8+.
* Credenciales de AWS configuradas (`~/.aws/credentials`) para acceso a DynamoDB.

### Pasos
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/jesuscastellanos/backendInvestmentFund.git
   cd backendInvestmentFund
   ```

2. Compilar el proyecto:
   ```bash
   mvn clean install
   ```

3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```

## 🧪 Pruebas Unitarias
Para ejecutar las pruebas y validar la lógica de negocio (incluyendo la validación de saldo insuficiente):
```bash
mvn test
```

## 📄 API Endpoints (Ejemplo)
* **Suscripción**: `POST /api/customers/{customerId}/subscribe`
    * **Body**: `{"fundId": "1"}`
    * **Respuesta de Error**: Si el saldo es menor al monto mínimo, retorna: 
        *"No tiene saldo disponible para vincularse al fondo <Nombre del fondo>"*

---
Desarrollado por **Jesus Castellanos** - 2026
