# Tasks

## 1. Server persistence

- [ ] 1.1 Add `server/feature-topics` with the topic model and the draft rule; verify with the model tests for the trim, the empty step, no title, no date and a zero duration
- [ ] 1.2 Add `V2__topics.sql` and the Exposed repository; verify with the PostgreSQL tests for create, read, update with fewer steps, the step order, the list order, the guest and the clean bootstrap (CI runs them)

## 2. Server admin API

- [ ] 2.1 Add the list, read, create and update use cases; verify with the use-case tests for the trimmed content, the rejected content, the unknown character and the unknown topic
- [ ] 2.2 Add the topic DTOs to `contract-admin`, the routes, and mount them in `app` behind the admin key; verify with the route tests for 200, 201, 400, 401 and 404

## 3. Admin data layer

- [ ] 3.1 Add the admin topics repository; verify with mock engine tests for 200, 201, 400, a call without saved settings and a failed call

## 4. Admin Topics

- [ ] 4.1 Add Topics to the sidebar before Characters and open the admin app on Topics; verify with the view-model tests for both start screens
- [ ] 4.2 Add `GetTopicsUseCase`, `TopicsViewModel`, the Topics screen and the actions slot of the toolbar; verify with the mapper and view-model tests for the rows, the empty list, the load error, Retry and a return

## 5. Admin Topic screen

- [ ] 5.1 Add `GetTopicUseCase`, `SaveTopicUseCase` and `TopicViewModel`; verify with the use-case test for create and update, and with the mapper and view-model tests for a new and a saved topic, Add step, Guest, the Saved status, the error for no title or date, the unread date and duration, the save failure and the load error
- [ ] 5.2 Add the Topic screen and open it from a row and from New topic; verify with `./gradlew build` and the images of the screen capture
- [ ] 5.3 Run the admin app against a server with a database; verify a create, an edit with Add step and Guest, and the topic after a server restart

## Workflow follow-up

- Run `/opsx:archive` as the last commit of the last PR for #7, so `openspec/specs/topics/spec.md` and `openspec/specs/characters/spec.md` update at the merge.
