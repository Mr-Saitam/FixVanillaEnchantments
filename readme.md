# FixVanillaEnchantments

<img width="1600" height="500" alt="banner" src="https://github.com/user-attachments/assets/6eb1d780-8abb-4013-9be6-bf14feff8a63" />


**Corrige automáticamente ítems con encantamientos ilegales — sin lag, sin comandos manuales, sin dramas.**

Sharpness X en una espada. Protection IV + Fire Protection IV en la misma armadura. Fortune y Silk Touch juntos en un pico. Si tu servidor alguna vez tuvo un exploit, un plugin mal configurado, o ítems importados de otro server, seguro tenés ítems así dando vueltas — y arruinan el balance para todos los demás.

FixVanillaEnchantments los detecta y corrige solo, en el momento en que aparecen: al conectarse, al moverlos en el inventario, al recogerlos del suelo, e incluso **dentro de shulker boxes y contenedores** — todo diseñado para no generar carga extra en el servidor.

<img width="1400" height="660" alt="before-after" src="https://github.com/user-attachments/assets/96fb5604-8be8-4d10-b9f8-b53309e7d224" />


##  Características
<img width="1500" height="260" alt="feature-strip" src="https://github.com/user-attachments/assets/55353585-cd3a-4272-a165-10af4b69efbf" />


-  **Límites vanilla configurables** por encantamiento (Sharpness V, Protection IV, etc.)
-  **Detección de incompatibilidades** (Sharpness + Smite, Fortune + Silk Touch, Riptide + Loyalty...) con reglas de prioridad configurables
-  **Remueve encantamientos inválidos** para el tipo de ítem (ej. Aqua Affinity en una espada)
-  **Revisa shulker boxes** — el contenido se corrige junto con la caja, sin escanear el mundo
-  **Revisa cofres, barriles, ender chests, dispensers y droppers** al cerrarlos
-  **Corrección automática** en: join, click de inventario, drag, recoger del suelo, cambio de hotbar, intercambio de manos, cierre de inventario
-  **Comando manual** `/fixmyitems` para que el propio jugador corrija su inventario
-  **Logging asíncrono** a archivo — cero impacto en el hilo principal del servidor
-  **100% configurable**: activá/desactivá cada trigger, cada mensaje, y los límites de cada encantamiento por separado


##  Idioma 100% configurable

Todo texto que el plugin muestra o escribe (mensajes en el chat, líneas del log, hasta los mensajes de debug) sale de `plugins/FixVanillaEnchantments/lang/<idioma>.yml`, nunca hardcodeado en el código. Se elige con una sola línea en `config.yml`:

```yaml
language: es   # o "en"
```

Vienen incluidos español (`es`) e inglés (`en`). Para agregar otro idioma: copiá `lang/es.yml` a `lang/<código>.yml`, traducilo, y cambiá `language:` — no hace falta recompilar el plugin. Si a una traducción le falta una clave, el plugin usa automáticamente el inglés embebido como respaldo, así nunca se rompe por un mensaje faltante.

##  Instalación

1. Descargá el `.jar` y soltalo en la carpeta `plugins/` de tu servidor Paper/Spigot.
2. Reiniciá el servidor (o cargalo con tu plugin manager favorito).
3. Editá `plugins/FixVanillaEnchantments/config.yml` a gusto.
4. Listo — no requiere ninguna dependencia externa.

**Requiere:** Paper/Spigot 1.13 o superior · Java 8+

##  Comandos

| Comando | Alias | Permiso | Descripción |
|---|---|---|---|
| `/fixmyitems` | `/fixme`, `/fixitems` | `fixvanillaenchants.use` (default: todos) | Corrige manualmente el inventario del jugador |
| `/fixenchants-reload` | `/fixench-reload`, `/fereload` | `fixvanillaenchants.reload` (default: op) | Recarga la configuración sin reiniciar |

##  Configuración

```yaml
settings:
  notify-players: true
  fix-on-join: true
  fix-on-inventory-move: true
  fix-on-pickup: true
  remove-invalid-enchants: true
  enable-file-logging: true
  fix-shulker-contents: true
  fix-containers: true
  debug-mode: false

  enchantment-priority:
    - sharpness
    - protection
    - power
    # ...

limits:
  sharpness: 5
  protection: 4
  # ... todos los encantamientos vanilla

incompatible:
  protection: [fire_protection, blast_protection, projectile_protection]
  sharpness: [smite, bane_of_arthropods]
  # ...
```

Cada trigger se puede desactivar individualmente si preferís un enfoque más liviano o más agresivo.

##  Rendimiento

Diseñado explícitamente para servidores con mucha población:

- El logging es **asíncrono** (cola en memoria + flush periódico), nunca bloquea el hilo principal.
- Las shulker boxes se filtran por tipo de ítem antes de deserializar su NBT — cero costo para ítems que no son shulkers.
- Los contenedores (cofres, barriles, etc.) se revisan solo al **cerrarse**, nunca por un listener de hoppers ni por escaneo periódico del mundo.
- Los handlers de inventario descartan eventos irrelevantes antes de agendar cualquier tarea.

##  Compilar desde el código fuente

```bash
git clone https://github.com/<tu-usuario>/FixVanillaEnchantments.git
cd FixVanillaEnchantments
mvn clean package
```

El `.jar` final queda en `target/FixVanillaEnchantments-<version>.jar`.

##  Contribuciones

Issues y PRs son bienvenidos. Si encontrás un ítem que se te escapa al fix, abrí un issue con el tipo de ítem y el contexto (cofre, shulker, trade de aldeano, etc.).

##  Licencia

MIT — usalo, modificalo, redistribuilo libremente.
