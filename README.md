CiberLog 🛡️
📌 Descripción

CiberLog es una plataforma colaborativa enfocada en la ciberseguridad, donde cualquier persona puede compartir conocimiento creando posts organizados por colecciones y etiquetas.

El objetivo es que quienes aprenden, investigan o experimentan en este campo puedan publicar tutoriales, experiencias y descubrimientos de forma abierta, y que la comunidad pueda valorarlos e interactuar.

🚀 Funcionalidades principales

✍️ Creación de posts con título, contenido y etiquetas

📂 Colecciones para organizar publicaciones

👍 Sistema de likes para valorar contenido

💬 Comentarios anidados (responder comentarios con hilos)

🚩 Reportar posts sospechosos o inapropiados

🛠️ Panel de administración para moderar contenido y gestionar reportes

🔐 Gestión de usuarios (roles: usuario y administrador)

🌙 Modo oscuro por defecto

📊 Feed dinámico con secciones:

Más vistos 👀

Más gustados ❤️

Más nuevos 🆕

Más útiles 🛠️

🛠️ Tecnologías utilizadas

Java con Spring Boot

Maven para gestión de dependencias

MySQL como base de datos

HTML / CSS / JavaScript para la interfaz

Spring Security (si aplica para autenticación/autorización)

📥 Instalación y ejecución

Clonar el repositorio:

git clone https://github.com/Carlos-CBS/CiberLog.git
cd CiberLog


Configurar la conexión a MySQL en el archivo application.properties o application.yml:

spring.datasource.url=jdbc:mysql://localhost:3306/ciberlog
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update


Compilar el proyecto con Maven:

mvn clean install


Ejecutar la aplicación:

mvn spring-boot:run


Acceder desde el navegador en:

http://localhost:8080

📖 Uso básico

Registrarse o iniciar sesión

Crear posts con etiquetas y organizarlos en colecciones

Explorar el feed: más vistos, más gustados, más nuevos o más útiles

Dar likes y comentar en publicaciones (con hilos de comentarios)

Reportar contenido inapropiado

Administradores: gestionar reportes y moderar posts desde el Admin Panel
