# MinecraftOverlay

Steam-подобный overlay для Fabric/Minecraft 26.2.

Текущая версия: **0.0.2b**

Overlay открывается и закрывается сочетанием `Shift + Tab`. По умолчанию используются две клавиши:

- `Steam Overlay: Key 1` — левый `Shift`;
- `Steam Overlay: Key 2` — `Tab`.

Обе клавиши доступны в настройках управления Minecraft. Если одну из них назначить пустой, overlay будет открываться по оставшейся клавише. Пока overlay только слегка затемняет экран.

## Структура

- `src/main/java/com/i114rk/minecraftoverlay/MinecraftOverlay.java` — точка входа мода.
- `src/client/java/com/i114rk/minecraftoverlay/MinecraftOverlayClient.java` — клиентские клавиши и переключение overlay.
- `src/client/java/com/i114rk/minecraftoverlay/SteamOverlayScreen.java` — экран затемнения.
- `src/main/resources/fabric.mod.json` — описание мода для Fabric Loader.
- `src/client/resources/assets/minecraftoverlay/lang/` — названия клавиш в настройках.

## Правило версий

Каждое изменение проекта увеличивает patch-часть версии на `0.0.1b`:

`0.0.1b` → `0.0.2b` → `0.0.3b`
