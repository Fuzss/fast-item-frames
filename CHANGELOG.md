# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v21.11.0-1.21.11] - 2025-12-17

### Added

- Items in invisible item frames now appear larger than items in normal frames; the scale can be changed in the config
- Add a config option for removing the item render offset for invisible item frames (works great with custom item
  texture models)
- Add a config option for hiding the item name label when looking at an item frame holding an item with a custom name

### Changed

- Update to Minecraft 1.21.11

### Fixed

- Rotating items in invisible item frame blocks no longer makes them become visible again
