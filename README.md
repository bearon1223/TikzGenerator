# TikzGenerator

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project allows you to create TikZ graphics graphically and export them into valid latex code. This program can also import existing TikZ code, provided it is in the correct format, and display it for modification.

## Controls
- `h` to hide or unhide the grid
- `Escape` to hide and un-hide the side menu bar
- `+ or =` to zoom in
- `-` to zoom out
- `Middle Mouse Button`  for panning
- `Backspace` to reset the zoom and position
- `Shift + Backspace` to clear the drawing
- `Ctrl + Z` Undo's the latest action (last line in the tik code)
- `Enter` to finalize a bezier curve

## Things to Note
- There is no redo
- This project is still _very much_ a work in progress and these things are likely to change.
- The font is also not one-to-one with how LaTeX will render the font although an attempt was made it.
- When downloading the release versions, only the exe file is included but the code itself is compatible with macOS. 
You just need to download the code and run it in terminal using the gradle commands below.
- It will do Math Rendering just beware the rendering is also not one-to-one with how LaTeX will render it.
- Bezier Curves are moved by dragging the control points around manually. Only quadratic Beziers are implemented currently and others may be added later.

## Things that may be added
- Cubic Bezier Curves
- Fixing the Font so that it is one-to-one with how LaTeX will render it
- Colors
- Exporting to file (Plain text, TeX files, etc.)

## Gradle Commands

Recommended way to run this program through gradle `./gradlew lwjgl3:run clean`

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
