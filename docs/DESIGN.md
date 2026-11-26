# Toddler Toybox TV — Design Spec (v1.5.0)

## Target Audience & Privacy
- **Age Group:** Toddlers (2–4 years old) playing solo on Android TV.
- **Core Principle:** Instant cause-and-effect. Zero reading, zero fail states, zero ads, **100% offline** (no `INTERNET` permission in `AndroidManifest.xml`).
- **10-Foot Safety:** Overscan-safe margins (`48dp` horizontal, `27dp` vertical), bright storybook colors, thick gold focus highlights, and smooth GPU-accelerated animations.

## Home Screen — 6×4 Toybox Picker (24 Games) + Surprise Show
- Single-screen 6×4 grid of 24 illustrated tiles (zero scrolling).
- Pressing **`UP`** from the top row focuses **`▶ SURPRISE SHOW (10s DEMO)`** (also triggered via remote Play/Pause).
- Pressing **`BACK`** twice within 1.5s on the Home screen exits the app safely.

## Quick Switching Controls
- Inside any play mode, all directional and center keys trigger instant visual + audio feedback.
- A **single tap of `BACK`** inside any game or Demo Mode immediately returns to the 24-game Toybox Picker so switching games is effortless.
- Volume keys always pass through to Android TV.

## Surprise Show (10s Auto-Play Demo Mode)
- Picks a random game (never repeating the same game twice in a row) and plays it for **10 seconds** with a subtle top-right countdown badge.
- Auto-triggers tailored beats (`650 ms` for musical/action/firecracker modes; `1300 ms` for voice/nature modes so spoken words and sounds finish cleanly).
- Live remote presses continue working alongside auto-play.

## The 24 Play Modes
1. **Balloons** — Floating balloons & stars with confetti & surprise stickers.
2. **Animals** — 8 cartoon animals with bounce dances, **real recorded animal sounds** & spoken names.
3. **Xylophone** — 5-bar C-major pentatonic xylophone + fireworks.
4. **Honk & Go** — 6 animated vehicles on a scrolling road with sirens, horns, steam & rocket flames.
5. **Songs** — 8-key auto-melody nursery rhyme piano (*Twinkle Twinkle*, *Happy Birthday*, *Mary Had a Little Lamb*, *Row Your Boat*).
6. **Fireworks** — Starry night sky with booming colorful firework rockets.
7. **Peek-a-Boo** — 4 hiding spots and 5 giggling friends.
8. **Shapes** — 6 smiling shapes × 7 bright colors.
9. **A to Z** — Full 26-letter alphabet explorer (`A a`–`Z z`) with phonics words & repeat (`DOWN`).
10. **1 to 100** — Counting 1 through 100 with spoken English words, star visualizers, milestone fireworks every 10, and `+10` jump (`UP`).
11. **Drums** — 5-piece cartoon drum kit with shockwave rings.
12. **Ocean** — 6 splashing sea creatures with rising bubbles.
13. **Weather** — Sunny, Rainy, Snowy, Rainbow, and Starry Night skies.
14. **Dinos** — T-Rex, Brontosaurus, Stegosaurus, Triceratops, and hatching Baby Dino Egg.
15. **Yummy** — Apple, Mango, Watermelon, Ice Cream, Cupcake, and Donut with crunchy bites.
16. **Paint** — Colorful paint-splatter art canvas (`DOWN` clears when full).
17. **Space** — Earth, Moon, Saturn, Mars, Sun, and Friendly Alien in a starfield.
18. **Garden** — Blooming flowers releasing fluttering rainbow butterflies.
19. **Chakkar** — Spinning Diwali ground-wheel throwing spiraling spark arcs.
20. **Anar** — 5 Diwali flower-pot cones shooting golden & neon spark fountains (`OK` = all 5).
21. **Sparkler** — Glowing phuljhari sparkler tracing neon hearts, stars, spirals, loops & rainbows.
22. **Birds** — Real recordings of Bluebird, Forest Robins, Crow, Night Owl, Duck, and Bumblebee.
23. **Silly SFX** — Real cartoon soundboard (Boing, Cork Pop, Cowbell, Bugle, Mouse, Wood Clack, Siren).
24. **Nature** — Real Ocean Waves, Rain Shower, Thunder Boom, Forest Morning, and Night Owls.
