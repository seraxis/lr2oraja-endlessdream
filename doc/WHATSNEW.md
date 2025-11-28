# Endless Dream 0.3.1
## New features
- #### Context Menu
  - Keys 3 and 5 now activate the Context Menu
  - The Context Menu can be opened on a Song or top-level Table folder in Music Select
  - Autoplay and Practice Mode functionality has been placed inside the Context Menu
  - The Context Menu can display all tables that a chart is found in and allows you to manage favourites among other features
- #### LR2IR and LR2 G-BATTLE Support
  - Lunatic Rave 2 Internet Ranking song leaderboards are now accessible inside the Context Menu
  - Pressing play on a LR2IR leaderboard score will enter G-BATTLE
    - Your pacemaker will be set to challenge the leaderboard score you have selected
    - With the RANDOM option enabled your random will match the random that the leaderboard score was obtained with
    - Only 7K is currently supported
    - Currently only NONRAN, MIRROR, and RANDOM are supported.
- #### In-game skin configuration
  - Update your skin settings live inside the game
    - Accessible via the Skin Configuration window in the mod menu
    - The menu will let you configure the currently displayed scene's skin configuration
      - To edit the PLAY scene, play a chart with the menu open. The same goes for RESULT, etc.
      - You can freeze gameplay timers, very useful for editing scenes like DECIDE
  - Edit skin elements dimensions and properties live with the Skin Widget Manager
    - Make small edits to your skin on the fly, resize elements with the mouse
    - Comes with an undo button and full history for all alterations
- #### OBS Scene switcher and Automatic Recording
  - Automate scene switching from within the Endless Dream launcher
  - Configure replay recording and saving with per-scene settings
    - Use the Load Scenes button once to set up your scene transitions and recording points. Subsequent launches will use the last used settings.
    - Recordings can be saved: always, whenever you take a screenshot, or whenever a replay is saved (Using the auto-save replay feature)
- #### Automatically send screenshots to Discord with Webhooks
  - Send a plain image or a rich embed to as many channels as you'd like with webhooks as soon as you take a screenshot
  - Add as many channels as you'd like, configurable from the Discord tab in the launcher
- #### Additional features
  - Automatic per-chart volume normalization has been added to the Audio tab in the launcher
  - An option to skip the DECIDE screen has been added to the Music Select tab in the launcher
  - You can force visual LN end caps to display in the Play Option tab in the launcher
  - A list of default table URLs is provided in a new table in the Resource tab in the launcher
    - You can quickly add popular tables from the Available table list into your Active table list
    - You still need to reload your tables for changes to take effect
  - The Misc Settings mod menu window has more play settings that previously required a restart to configure
  - Song Manager window in the mod menu now has an option to sort songs by least recently played
  - The Konmai chart download source can now be used as an alternative to Wriggle
  - German language translations of the launcher are now available

## Behavior changes
- Playing courses with CONSTANT is now ASSIST CLEAR (previously CONSTANT had no effect)
- Switching to CN/HCN LN modes has been disabled in game. This can now only be changed in the launcher
- Notifications will now be displayed for a variety of different events
  - E.g. when downloading songs, playing a song with options that will restrict score saving, etc.
- The analog scratch threshold default has been changed from 100 to 50
- The spinning turntable emblem in skins now spins smoother
- The sound of quickly scrolling through music select is less loud
- Improved audio when changing between scenes
- Music previews generated with the preview generator tool will have lower priority
- Skins with large bitmap fonts will load faster
- In-progress song downloads now display a progress bar
- Loading BMS and Table will now display a spinner instead of hanging the launcher
- Song Downloader menu has a retry failed downloads button
- Config saving and loading has gotten more robust
- Your settings will now save periodically when you have the game open
- FPS 0 now results in an FPS of 1000 which should reduce stutters, for a higher FPS target the target must be input manually
- The Controller Debug Information dropdown has been renamed and contains Endless Dream build information along with its previous contents
- Any URL can now be used for IPFS song fetching

## Bug fixes
- Fixed the Linux Wayland crash with NVIDIA graphics cards (no more gamescope!)
- Fixed a crash when launching borderless without a set monitor
- Fixed issues with loading osu files
- On Linux, fixed opening chart folder with F3, the new version download link in the launcher, and fixed the launcher becoming non-functional after starting the game
- Fixed Discord rich presence on macOS
- Fixed bitmap font text display becoming transparent in certain contexts
- Fixed a crash caused by incorrect entries in some difficulty tables
- Fixed the Fullscreen toggle button (F4) causing skins to behave incorrectly and making the window bar inaccessible

## Known issues:
- [Linux] Certain skin fonts may only load partially due to incorrect letter case in their filenames. Can be manually resolved by renaming the offending files.
- [Linux] When loading configuration files created on Windows, skin settings will fail to transfer. Fix by replacing backslashes with forward slashes in the skin paths in saved skin settings in `config.json` and `config_play.json` in the player folder.
- Switching skins in the Skin Configuration menu can leave artifacts (floating text, big bar lines, etc.) on some skins after switching. This currently requires a restart to resolve.
- Chart downloads can occasionally lock the database, requiring a manual restart and a folder refresh (F2) on the HTTP download folder.
- Skin Widget Manager works abnormally when editing sliders or scrollbars
