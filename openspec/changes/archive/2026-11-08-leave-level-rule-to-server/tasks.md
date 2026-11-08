## 1. Levels leaves the level rule to the server

- [x] 1.1 Keep the load facts in the Levels `DataState`: the levels, `null` until they load, and the
  load failure. Let the mapper decide Loading, Error and a list without all seven levels.
- [x] 1.2 Move the form to the domain as text input, and the conversion of a level to a form to a
  mapper.
- [x] 1.3 Read only the numbers and the idioms choice before a save. Remove the local level rule.
- [x] 1.4 Update the view-model and mapper tests and the Levels captures.
