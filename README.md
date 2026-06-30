# Vigía 🛰️

**Monitoreo ambiental comunitario** cerca de faenas mineras. Vigía acerca a las comunidades el estado del **agua, el aire, la tierra y el ruido** de su sector, con un lenguaje claro y datos abiertos.

> Proyecto del ramo **Innovación y Emprendimiento II**. App Android nativa (Kotlin + Jetpack Compose).

<p align="center">
  <a href="https://github.com/PandaAkiraNakai/Innovacion-y-Emprendimiento-II/releases/latest/download/Vigia.apk">
    <img src="https://img.shields.io/badge/⬇%20Descargar-APK-466B4E?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK">
  </a>
  &nbsp;
  <img src="https://img.shields.io/github/v/release/PandaAkiraNakai/Innovacion-y-Emprendimiento-II?style=for-the-badge&color=B5713B&label=versi%C3%B3n" alt="Versión">
</p>

---

## 📲 Descarga rápida

Toca el botón de arriba o entra a **[Releases](https://github.com/PandaAkiraNakai/Innovacion-y-Emprendimiento-II/releases/latest)** y baja el archivo `Vigia.apk` directo a tu teléfono.

> [!NOTE]
> Al instalar puede que Android te pida permitir "instalar apps de orígenes desconocidos". Es normal en apps que no vienen de la tienda. Requiere **Android 8.0 (API 26) o superior**.

---

## ¿Qué hace?

Vigía es una **demostración funcional**: simula de forma realista lo que haría la app conectada a sensores reales, ideal para presentarla. Todos los datos se generan en el dispositivo; no se conecta a internet ni a sensores físicos.

### 4 zonas mineras de ejemplo, seleccionables

Cada zona tiene su **propio territorio, estaciones, mapa, alertas y datos**, y un perfil ambiental distinto:

| Zona | Faena | Desafío principal |
|------|-------|-------------------|
| **Mina Cerro Bayo** | Cobre · tajo abierto | 💧 Calidad del **agua** (metales y acidez) |
| **Faena El Algarrobo** | Hierro · tajo abierto | 🌬️ **Aire** (polvo en suspensión) |
| **Mina Quebrada Honda** | Oro y plata · subterránea | 🔊 **Ruido** (tronaduras y camiones) |
| **Planta Llano Verde** | Litio y sales · planta | 🌱 Mayormente en norma, vigilando el **suelo** |

Cambia de zona desde el selector superior y toda la app —indicadores, sensores, mapa, alertas y datos— se actualiza al instante.

### 4 dominios ambientales

- **💧 Agua** — arsénico, cobre, pH, turbidez, sulfatos.
- **🌬️ Aire** — PM2.5, PM10, SO₂, NO₂, polvo sedimentable.
- **🌱 Tierra** — plomo, arsénico, cobre, pH y humedad del suelo.
- **🔊 Ruido** — diurno, nocturno, pico máximo, tronaduras.

Cada medición se compara con su **límite normativo** y se clasifica en **Normal**, **Vigilancia** o **Crítico**.

## Pantallas

- **Resumen** — estado general de la zona, indicadores clave, tarjetas por sensor con tendencia y alertas recientes.
- **Mapa** — las estaciones de monitoreo sobre el territorio; toca una estación para ver su estado y sus alertas.
- **Alertas** — centro de avisos de la zona, con filtros por dominio y estado (activa, reconocida, resuelta).
- **Sensor (detalle)** — medidor radial, evolución 24 h / 7 d / 30 d, todas las mediciones y contexto del dominio.
- **Datos abiertos** — descarga los registros en **CSV o JSON** (se generan en el dispositivo y se comparten).
- **Perfil** — preferencias de alertas y envío de reportes ciudadanos.

## 🧱 Stack

- **Kotlin** + **Jetpack Compose** (Material 3), navegación con Navigation Compose.
- Gráficos (área, sparkline, medidor radial) y mapas esquemáticos dibujados a mano con **Canvas**.
- Tipografías variables: Bricolage Grotesque, Hanken Grotesk y Spline Sans Mono.
- Datos de demostración deterministas (semilla fija) para una presentación estable.
- Sin dependencias de red ni permisos sensibles.

## 🛠️ Compilar desde el código

Requisitos: **JDK 17+** y el **Android SDK** (API 35).

```bash
git clone https://github.com/PandaAkiraNakai/Innovacion-y-Emprendimiento-II.git
cd Innovacion-y-Emprendimiento-II

# APK de release (instalable)
./gradlew assembleRelease
# queda en app/build/outputs/apk/release/app-release.apk

# o instálalo directo en un teléfono conectado
./gradlew installRelease
```

> El release se firma con la clave de depuración para que el APK sea instalable sin gestionar un keystore aparte. Es una decisión a propósito por tratarse de una demostración.

## Nota

App de demostración de interfaz: los datos son simulados y los lugares, faenas y personas son ficticios.
