# MinecraftOverlay

Steam-подобный overlay для Fabric/Minecraft 26.2.

Текущая версия: **0.0.3b**

Overlay открывается и закрывается сочетанием `Shift + Tab`. По умолчанию используются две клавиши:

- `Steam Overlay: Key 1` — левый `Shift`;
- `Steam Overlay: Key 2` — `Tab`.

Обе клавиши доступны в настройках управления Minecraft. Если одну из них назначить пустой, overlay будет открываться по оставшейся клавише.

В overlay слева сверху отображаются текущее время и FPS. ПКМ по FPS открывает действие **«Закрепить»** — закреплённый FPS остаётся видимым во время игры после закрытия overlay. ПКМ в другом месте overlay открывает **«Редактировать макет»**; после выбора FPS можно перетащить мышью.

## Структура

- `src/main/java/com/i114rk/minecraftoverlay/MinecraftOverlay.java` — точка входа мода.
- `src/client/java/com/i114rk/minecraftoverlay/MinecraftOverlayClient.java` — клиентские клавиши и переключение overlay.
- `src/client/java/com/i114rk/minecraftoverlay/SteamOverlayScreen.java` — экран затемнения.
- `config/minecraftoverlay.properties` — сохранённое состояние закрепления и позиция FPS.
- `src/main/resources/fabric.mod.json` — описание мода для Fabric Loader.
- `src/client/resources/assets/minecraftoverlay/lang/` — названия клавиш в настройках.

## Правило версий

Каждое изменение проекта увеличивает patch-часть версии на `0.0.1b`:

`0.0.1b` → `0.0.2b` → `0.0.3b`
