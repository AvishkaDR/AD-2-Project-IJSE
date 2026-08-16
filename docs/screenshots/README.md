# Screenshots Directory

Save your live Eureka dashboard screenshot in this directory with the file name `eureka_dashboard.png`.

### Steps to Capture:
1. Start `config-server` (Port 8888).
2. Start `eureka-server` (Port 8761).
3. Start `user-service` (Port 8081).
4. Start `vehicle-service` (Port 8082).
5. Start `parking-space-service` (Port 8083).
6. Start `payment-service` (Port 8084).
7. Start `api-gateway` (Port 8080).
8. Open your browser and navigate to `http://localhost:8761`.
9. Ensure all instances (`API-GATEWAY`, `USER-SERVICE`, `VEHICLE-SERVICE`, `PARKING-SPACE-SERVICE`, `PAYMENT-SERVICE`) are listed under **"Instances currently registered with Eureka"**.
10. Take a full-window screenshot and save it as `docs/screenshots/eureka_dashboard.png`.
