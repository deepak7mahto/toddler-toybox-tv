# Toddler Toybox TV

A solo, 100% offline, zero-permission, ad-free Android TV play app designed for toddlers (ages 2–4). Built with Kotlin, Jetpack Compose Canvas, a procedural PCM audio synthesizer, and 21 `ffmpeg`-optimized real animal/bird/nature/cartoon recordings (`1.1 MB` R8 release APK).

## 24 Built-In Toddler Games (6×4 Single-Screen TV Grid)
1. **Balloons** — Floating smiling balloons & golden stars that pop into confetti and surprise stickers.
2. **Animals** — 8 giant vector-drawn animated animals (Lion, Elephant, Duck, Cow, Puppy, Frog, Kitty, Owl) with **real animal sound recordings**.
3. **Xylophone** — 5-bar pentatonic rainbow xylophone (C–D–E–G–A) with fireworks.
4. **Honk & Go** — 6 animated vehicles (Fire Truck, School Bus, Train, Police Car, Rocket, Tractor).
5. **Songs** — 8-key rainbow piano that plays *Twinkle Twinkle*, *Happy Birthday*, *Mary Had a Little Lamb*, and *Row Your Boat* one note per press.
6. **Fireworks** — Starry night sky where every press launches colorful booming rockets & multi-ring fireworks.
7. **Peek-a-Boo** — Big hiding spots (Gift Box, Cloud, Bush, Barn) with popping friends shouting *"Peek-a-boo!"*.
8. **Shapes** — 6 giant smiling shapes × 7 bright colors that bounce and speak their color & shape.
9. **A to Z** — Dedicated alphabet explorer (`A a` through `Z z`, *"A for Apple"* to *"Z for Zebra"*).
10. **1 to 100** — Dedicated counting game from `1` to `100` with spoken words, visual star counters, milestone fireworks every 10, and `+10` jump on `UP`.
11. **Drums** — 5-piece colorful cartoon drum kit (Snare, Cymbal, Bass, Tom, Bongo) with shockwave rings.
12. **Ocean** — Underwater bubbles & 6 splashing sea creatures (Whale, Octopus, Clownfish, Sea Turtle, Starfish, Happy Crab).
13. **Weather** — Switch between Smiling Sun, Raindrops & Umbrella, Snowman, Giant Rainbow, and Starry Night.
14. **Dinos** — Friendly T-Rex, Brontosaurus, Stegosaurus, Triceratops, and a hatching Baby Dino Egg.
15. **Yummy** — Smiling Apple, Mango, Watermelon, Ice Cream, Cupcake, and Donut with crunchy *"Crunch! Yummy!"* bites.
16. **Paint** — Giant blank canvas where every press splatters colorful paint blobs & stars (`DOWN` clears when full).
17. **Space** — Drifting starfield visiting Earth, Moon, Saturn's rings, Mars, Big Sun, and a Friendly Alien in a UFO.
18. **Garden** — 5 blooming flowers that release fluttering rainbow butterflies & harp sparkles.
19. **Chakkar** — Diwali ground-spinner wheel throwing spiraling rainbow & gold spark arcs faster on every press.
20. **Anar** — 5 colorful Diwali flower-pot cones shooting towering golden & neon spark fountains (`OK` ignites all 5!).
21. **Sparkler** — Glowing phuljhari sparkler wand tracing neon hearts, stars, spirals, loops, and rainbows across the night sky.
22. **Birds** — Real recordings of Bluebird, Forest Robins, Crow, Night Owl, Little Duck, and Bumblebee fluttering on a tree branch.
23. **Silly SFX** — Real cartoon soundboard (Spring Boing, Cork Pop, Cowbell, Trumpet Bugle, Squeaky Mouse, Wood Clack, Siren) with squash-and-stretch animations.
24. **Nature** — Real Ocean Waves, Rain Shower, Thunder Boom, Forest Morning, and Night Owls.

## Surprise Show (10s Auto-Play Demo Mode)
- Press **`UP`** from the top row on Home to highlight **`▶ SURPRISE SHOW (10s DEMO)`** and press **`OK`** (or hit **Play/Pause** on the TV remote).
- Automatically plays a random game for **10 seconds** (auto-triggering tailored actions/melodies while still accepting live remote presses), then switches to a new random game without repeating.

## Remote Controls
- **Inside any game / Demo Mode:** Every D-pad & OK press triggers instant fun; a **single tap of `BACK`** immediately returns to the 24-game Toybox menu.
- **On Home:** Press **`BACK` twice** within 1.5s to exit the app safely.

## Documentation
- [docs/DESIGN.md](docs/DESIGN.md) — Product design, 24 play modes, Demo Mode, and remote controls.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Module layout, 60fps Compose Canvas loop, hybrid PCM + real MP3 audio engine, and performance optimizations.
- [docs/BUILD_AND_DEPLOY.md](docs/BUILD_AND_DEPLOY.md) — Building R8 release APKs, ART AOT compilation, and sideloading to Android TVs via ADB.
