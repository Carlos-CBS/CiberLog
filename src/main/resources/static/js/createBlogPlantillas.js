// Gestión de plantillas
document.querySelectorAll('.template-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        const templateType = this.dataset.template;
        let templateContent = '';

        switch(templateType) {
            case 'vulnerability':
                templateContent = `# Análisis de Vulnerabilidad: [NOMBRE]

## Descripción
Breve descripción de la vulnerabilidad.

## Detalles Técnicos
* **Identificador**: CVE-20XX-XXXXX
* **Gravedad**: Alta/Media/Baja
* **Sistemas Afectados**: 
* **Vector de Ataque**: 

## Análisis Técnico
Explicación detallada de la vulnerabilidad...

## Prueba de Concepto
\`\`\`python
# Código de ejemplo
\`\`\`

## Mitigación
Pasos para mitigar la vulnerabilidad...

## Referencias
* [Link a referencia 1]()
* [Link a referencia 2]()`;
                break;
            case 'tutorial':
                templateContent = `# Tutorial: [TÍTULO]

## Introducción
Breve descripción de lo que vamos a aprender.

## Requisitos Previos
* Requisito 1
* Requisito 2

## Paso 1: [Descripción]
Explicación detallada...

\`\`\`bash
# Comando de ejemplo
\`\`\`

## Paso 2: [Descripción]
Explicación detallada...

## Resultados Esperados
Lo que deberíamos conseguir al final del tutorial...

## Medidas de Seguridad
Consideraciones de seguridad importantes...

## Conclusión
Resumen y siguientes pasos.`;
                break;
            case 'news':
                templateContent = `# [TITULAR DE LA NOTICIA]

**Fecha**: [FECHA]

## Resumen
Breve resumen de la noticia de seguridad...

## Contexto
Información contextual relevante...

## Impacto
* Sector afectado 1
* Sector afectado 2

## Análisis Técnico
Detalles técnicos si están disponibles...

## Recomendaciones
Qué medidas deberían tomar los usuarios o empresas...

## Fuentes
* [Fuente 1]()
* [Fuente 2]()`;
                break;
            case 'poc':
                templateContent = `# Prueba de Concepto: [TÍTULO]

> ⚠️ **Advertencia**: Esta prueba de concepto es únicamente para fines educativos y de investigación.

## Objetivo
Explicación del objetivo de esta prueba de concepto...

## Entorno de Pruebas
* SO: 
* Aplicación: 
* Versión: 

## Explicación Técnica
Detalles técnicos de la vulnerabilidad o técnica...

## Código de la Prueba de Concepto
\`\`\`python
# Código PoC
import requests

# Resto del código...
\`\`\`

## Resultados
Resultados esperados y capturas de pantalla...

## Mitigación
Cómo protegerse contra esta vulnerabilidad...

## Referencias
* [Referencia 1]()
* [Referencia 2]()`;
                break;
        }

        simplemde.value(templateContent);
        // Disparar el evento change para actualizar la vista previa
        simplemde.codemirror.getDoc().setValue(templateContent);
    });
});