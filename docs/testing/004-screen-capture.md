# Screen Capture

A capture renders each screen of an app, in each of its states, to an image. A person or the
`ui-review` agent compares the images with the design canvas. Each app has one capture method,
and this guide lists it.

## Rules

- Capture every state that the UI state of a screen can hold: loading, empty, content, error, and
  each inline status, such as a save status.
- Build each state through the real UI state mapper, so the images show the real copy.
- Put the hard cases into the data: a long name, a long text, several rows.
- Name an image `<screen>-<state>.png`.
- The images are build output. Do not commit them.
- When you add a screen or a state, add it to the capture in the same change.

## Admin

`ScreenCaptureTest` renders each admin screen inside the sidebar frame, at 1280 × 800, and writes
the images to `admin/build/screens/`:

```bash
./gradlew :admin:test --tests 'app.yap.admin.ScreenCaptureTest'
```

The build runs it too, so a state that does not render fails the build.

## Mobile

The mobile app has no capture method yet. Add it to this guide together with its first screen.
Give `ui-review` the tools of that method in the same change, and say in the method how it
restores the device settings that it changes.
