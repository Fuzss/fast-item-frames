# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v21.11.1-1.21.11] - 2025-12-25

### Added

- Item frames holding an item can now be waxed with honeycomb similarly to signs
- This prevents any changes, such as rotating the item and toggling the item frame invisibility state

### Changed

- Move most block properties from the internal item frame entity to the block state

### Removed

- Remove passing clicks through item frames to attached container blocks (like chests), which will come back in a
  separate more feature complete mod

## [v21.11.0-1.21.11] - 2025-12-17

### Added

- Clicking on an item frame attached to a container block now interacts with that block instead of the item frame in
  most cases (this behavior is configurable)
- Items in invisible item frames now appear larger than items in normal frames; the scale can be changed in the config
- Add a config option for removing the item render offset for invisible item frames (works great with custom item
  texture models known as CIT)
- Add a config option for hiding the item name label when looking at an item frame holding an item with a custom name

### Changed

- Update to Minecraft 1.21.11
- Existing vanilla item frame entities are no longer converted automatically upon loading the chunks; instead, only
  newly placed item frames will appear as blocks (this behavior is configurable)

### Fixed

- Rotating items in invisible item frame blocks no longer makes them become visible again
