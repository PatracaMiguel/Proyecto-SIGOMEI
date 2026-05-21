# Proyecto-SIGOMEI
Proyecto de Desarrollo de sistemas en red y Taller de pruebas de software 
Este repo contiene la estuctura del servidor SIGOMEI y las pruebas unitarias en etapa roja del TDD 

## Tecnologías
- Java 21
- maven 
- JUnit 5
- RMI 
- MySQL
- H2 para pruebas


## Estructura del proyecto
- `api/dto`: objetos de transferencia de datos.
- `api/catalogos`: valores fijos del sistema.
- `api/excepciones`: excepciones del sistema.
- `servidor/rmi`: contrato RMI.
- `servidor/service`: capa de servicio sin lógica.
- `servidor/repository`: interfaces de persistencia.
- `src/test`: pruebas unitarias RN-01 a RN-08.

## Para ejecutar pruebas 
Entra en la ruta proyecto_Sigomei\Proyecto-SIGOMEI\proyecto
Ejecuta `mvn clean test`

## Para la conexion a la base de datos
Entra a la ruta src/main/java/com/sigomei/servidor/config/PruebaConexion.java
y selecciona Run Java 