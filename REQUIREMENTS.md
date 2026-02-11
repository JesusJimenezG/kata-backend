Fullstack Resource Management and Reservation Platform
Desafío fullstack para crear una plataforma de gestión y reserva de recursos compartidos. Este
documento define objetivos, alcances, entregables y criterios de calidad.
[Fullstack] Plataforma de Gestión y Reserva de Recursos Compartidos
Descripción
Construye una aplicación fullstack que permita a equipos reservar y administrar recursos
compartidos (salas, equipos, vehículos, etc.), evitando solapamientos, mostrando disponibilidad y
ofreciendo historial de reservas.
Puedes utilizar herramientas o asistentes de IA en cualquier fase (código, documentación, pruebas,
etc.). Indica de forma explícita en el README cómo y dónde las utilizaste, junto con tu criterio para
aceptar o modificar sugerencias.
Tiempo estimado
4 horas de implementación enfocada.
Tecnologías obligatorias
Frontend web o mobile (siempre en TypeScript)
·
Elige uno: React, Next.js, Angular o React Native.
Backend (API RESTful obligatoria, elige una opción)
·
Node.js con TypeScript y Express
•
NestJS (TypeScript)
Java con Spring Boot
Base de datos (elige una)
•
PostgreSQL
•
MongoDB
DynamoDB
Requisitos funcionales mínimos
Autenticación de usuarios
•
Registro y login (mínimo email y contraseña). Almacenar contraseña cifrada. Sesiones con JWT o
equivalente.
·
Roles básicos: usuario y administrador.
Gestión de recursos
•
CRUD completo: alta, edición, baja, listado y detalle.
• Validar nombres únicos por recurso (sin duplicados).
Reservas y disponibilidad
•
Crear reservas indicando fecha/hora de inicio y fin para un recurso.
• Prevenir solapamientos para el mismo recurso en intervalos coincidentes.
·
Listados de reservas activas por usuario y globales.
•
Cancelar reserva: permitido al creador o al administrador.
Vista rápida de disponibilidad
Implementar tabla o calendario simple que muestre franjas disponibles y reservadas por recurso.
Historial de reservas
Consultar el historial pasado de reservas por recurso.
API documentada
Describir endpoints (en README, Swagger, Postman u otra herramienta).
Extras valorados (opcional)
Filtros de búsqueda por fecha, usuario o recurso.
• Notificaciones básicas (consola, email simulado o UI) al crear, eliminar o cancelar reservas.
•
Gráficos simples de ocupación (porcentaje de uso por día/semana).
•
Validaciones robustas en backend y feedback de errores claro en frontend.
·
Despliegue funcional (Vercel, Railway, Render, etc.).
Entregables
Repositorio público en GitHub y README completo (cómo ejecutar y cómo probar).
•
Pruebas unitarias para: autenticación (login correcto e incorrecto), creación de recurso, creación
de reserva y prevención de solapamientos.
•
Explicación breve de arquitectura y decisiones tecnológicas en el README.
•
Sección detallando el uso de IA y el criterio aplicado para aceptar o editar sugerencias.
Consideraciones
. Frontend obligatoriamente en TypeScript.
•
Backend en Node.js (TypeScript), NestJS o Java Spring Boot.
·
Debe funcionar localmente; incluye instrucciones claras en el README.
Se permite scaffolding/generadores, pero se espera personalización y justificación arquitectónica.
•
Prioriza cumplir los mínimos con claridad y robustez antes de añadir extras.
✔ ¡Éxitos! Buscamos una solución bien diseñada, funcional y extensible.
