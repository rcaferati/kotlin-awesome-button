# Agentic Awesome Button

## Core Description
Build a 3D button with three independent layers: a flat translucent shadow plane, a fixed darker bottom shell, and a moving face. The shadow plane is 98% of the shell width, centered horizontally, positioned `raise / 2` below the shell at rest, and translated upward by `(raise / 2) * pressValue` while pressed. The moving face translates downward by `raise * pressValue`. `pressValue` starts at `0`, animates to `1` on press-in, and returns to `0` on release.

Center the main content and support optional `before`, `after`, and `extra` slots, auto/fixed/stretch widths, disabled/busy states, and theming for face/shell/shadow/active/progress colors, radius, padding, and typography. Successful taps dispatch once; canceled drag-out or tap-out releases visually without dispatching. Disabled buttons do not arm or dispatch.

Progress mode is one-shot and re-entry-blocked: on the first valid tap, keep the button visually pressed, elastically scale and fade content out while scaling and fading the spinner in over 300ms, and show a clipped full-width progress layer translating left to right behind content and spinner over `progressLoadingTime` (default 3000ms). When `next()` is called, complete the bar over 200ms, wait 100ms before fading the loading overlay out over 200ms, restore content, then release and unlock the button. Preserve button role, enabled/busy semantics, and accessibility throughout.

This descriptor covers the shared surface of `react-native-awesome-button` and `flutter_awesome_button`. When the implementations diverge, use the host implementation's public prop names and defaults instead of averaging them.

## Visual Architecture
- Shadow plane:
  - flat translucent layer
  - width is 98% of the face and bottom shell width
  - centered horizontally
  - rest bottom offset is `-raise / 2`
  - press transform is `translateY: -(raise / 2) * pressValue`
- Bottom shell:
  - fixed darker layer
  - full face width
  - remains stationary during press and progress
- Face:
  - full interactive surface
  - press transform is `translateY: raise * pressValue`
  - contains active background, progress layer, content, and activity indicator
- Geometry mapping:
  - React Native: public `height` is the stack height before padding; container height is `height + paddingTop + paddingBottom`; face/bottom/progress/active/content height is `height + paddingTop + paddingBottom - raiseLevel`; shadow height is `height - raiseLevel`
  - Flutter: public `height` is the moving face height; total shell height is `height + raiseAmount`; shadow height is `max(0, height - raiseAmount)`
  - Neutral generation target: if a target has no established convention, model face height as `H`, total stack height as `H + raise`, and shadow height as `max(0, H - raise)`

## Interaction Model
- Press feedback starts immediately on pointer/touch down.
- Press-in state:
  - React Native animates `pressValue` to `1` over 100ms with `Easing.out(Easing.cubic)`
  - Flutter animates `pressValue` to `1` with the resolved style duration and curve; fallback duration is 140ms and fallback curve is `Curves.easeOutCubic`
  - active face background opacity tracks `pressValue` from `0` to `1`
  - React Native defaults active background to `rgba(0, 0, 0, 0.15)`
  - Flutter uses `backgroundActive` when provided; otherwise it alpha-blends `pressedOverlayColor` over `backgroundColor`; fallback `pressedOverlayColor` is `Color(0x14000000)`
  - non-progress buttons apply `activeOpacity` while pressed; default is `1`
- Release state:
  - React Native defaults to spring release with tension `100` and friction `6.75`; if `springRelease` is `false`, active background and face translation reverse over 100ms
  - Flutter release always uses a spring simulation with mass `1`, Origami tension `100`, and Origami friction `6.75`; those convert to stiffness `447.4` and damping `21.25`
  - the spring is underdamped and may overshoot rest before settling
- Dispatch rules:
  - successful tap dispatches once
  - canceled drag-out or tap-out releases visually but does not dispatch
  - disabled buttons do not arm or dispatch
  - busy progress buttons block re-entry until completion
- Progress buttons keep the pressed visual state through loading.

## Layout Model
- Main content is centered.
- `before` and `after` are inline slots rendered with the main content and should move/transition with it.
- `extra` is face-local background content:
  - React Native inserts `extra` as the first child inside the face container before the active background, main content, and activity indicator; a background-style `extra` must provide its own fill/absolute positioning
  - Flutter renders `extra` as a fill layer behind active/progress/content layers
- Supported sizing modes:
  - auto width, measured from content
  - fixed width
  - stretch/full available width
- Size changes animate over 125ms with cubic `(0.3, 0.05, 0.2, 1)` when size animation is enabled.
- Theming/configurable layout surface includes:
  - face, shell, shadow, active, and progress colors
  - border color and width
  - shared and per-corner radius
  - raise depth
  - horizontal and vertical content padding
  - foreground/text color
  - text size, line height, and optional font family

## Progress Model
- Progress is a one-shot flow entered by the first valid tap.
- Once accepted:
  - the button becomes busy
  - re-entry is blocked
  - content scales and fades out elastically
  - spinner scales and fades in elastically
  - the face stays visually pressed
- Progress bar rendering:
  - rendered when `showProgressBar` is enabled
  - full-width layer inside the clipped face
  - translated from offscreen left to aligned left, not resized by width growth
  - rendered behind content and spinner, above the active background
  - themeable through a progress color
  - React Native default progress color is `rgba(0, 0, 0, 0.15)`
  - Flutter fallback progress color is `Color.fromRGBO(0, 0, 0, 0.15)`
  - when `showProgressBar` is disabled, progress keeps the spinner but hides the progress fill and suppresses active-background darkening during loading
- Completion:
  - `next()` completes the progress layer to `1` over 200ms with an ease-out cubic curve
  - content returns and spinner exits over 300ms using elastic bounciness `1.2`
  - loading overlay waits 100ms, then fades out over 200ms with an ease-out cubic curve
  - only after that does the button release and unlock
- Guarding:
  - a progress button remains guarded until the completion sequence and release finish, not merely until finger-up
  - repeated `next()` calls after the first completion request are ignored

## Behavioral Constants
- Immediate press-in on down event
- Press value target: `1`
- Press translation: `raise * pressValue`
- Shadow rest offset: `-raise / 2`
- Shadow counter-shift: `-(raise / 2) * pressValue`
- Shadow width factor: `0.98`
- React Native press-in: 100ms, ease-out cubic
- Flutter fallback press-in: 140ms, ease-out cubic
- Content/spinner elastic swap: 300ms, bounciness `1.2`
- Default progress travel: 3000ms
- Progress completion fill: 200ms, ease-out cubic
- Loading overlay fade-out: 100ms delay followed by 200ms fade
- Size animation: 125ms, cubic `(0.3, 0.05, 0.2, 1)`
- Release spring: tension `100`, friction `6.75`; Flutter equivalent stiffness `447.4`, damping `21.25`
- React Native defaults: `height = 60`, `raiseLevel = 4`, `borderRadius = 4`, `borderWidth = 0`, `paddingHorizontal = 16`, `paddingTop = 0`, `paddingBottom = 0`, `textSize = 14`, `textLineHeight = 20`
- Flutter fallback defaults: `height = 52`, `raiseAmount = 6`, `borderRadius = 18`, `borderWidth = 0`, `paddingHorizontal = 16`, `paddingTop = 0`, `paddingBottom = 0`, `textSize = 14`, `textLineHeight = 20`

## Accessibility And Semantics
- Expose button semantics/role.
- Reflect enabled/disabled state correctly.
- Reflect busy/loading state during progress.
- Keep the control focusable and operable as a button throughout its lifecycle.
- Visual busy or disabled states must align with semantic busy/disabled states.
- React Native uses `accessibilityRole="button"` and merges `disabled`/`busy` into `accessibilityState`.
- Flutter uses `Semantics(button: true)`, disables semantic activation while disabled or busy, and sets semantic value to `Busy` while progress is active.
