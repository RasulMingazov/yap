## ADDED Requirements

### Requirement: A topic is a draft or published

A topic SHALL be a draft or published. Create SHALL make a draft. Publish SHALL make a draft
published. A published topic SHALL stay published: unpublish is out of scope. The server SHALL keep
the status in its database.

#### Scenario: A new topic

- **WHEN** the owner creates a topic
- **THEN** the server stores it as a draft

#### Scenario: Publish

- **WHEN** the owner publishes a draft that has every field, and the server restarts
- **THEN** the admin API returns the topic as published

### Requirement: Publishing needs every field

The server SHALL publish a topic only when it meets the draft rule and has a duration, a
description, a setting, an interlocutor, a role, the opening line, its translation and at least one
step. A guest SHALL have a description. Each step SHALL have its task, Pass when and outcome. The
server SHALL name each missing field, and the numbers of the steps that are not complete.

#### Scenario: A topic with every field

- **WHEN** the owner publishes a topic with every field and two complete steps
- **THEN** the server publishes it

#### Scenario: Missing fields

- **WHEN** the owner publishes a topic without a setting, whose steps 3 and 5 have no outcome
- **THEN** the server publishes nothing and names Setting and the steps 3 and 5

#### Scenario: No steps

- **WHEN** the owner publishes a topic without steps
- **THEN** the server publishes nothing and names Steps

#### Scenario: A guest without a description

- **WHEN** the owner publishes a topic with a guest whose description is empty
- **THEN** the server publishes nothing and names Interlocutor

### Requirement: A release date in the past

The server SHALL NOT publish a draft with a release date before today, and SHALL NOT save a
published topic with a new release date before today. It SHALL name the past date as what the
topic still needs. A published topic that keeps its stored past date SHALL stay valid.

#### Scenario: A draft with a past date

- **WHEN** today is 23 October, and the owner publishes a draft with the date 22 October
- **THEN** the server publishes nothing and names the past date

#### Scenario: A topic that is out

- **WHEN** a published topic with the date 22 October is out, and the owner saves it with a new
  title and the same date
- **THEN** the server stores the title

#### Scenario: A move into the past

- **WHEN** today is 23 October, and the owner saves a published topic of 25 October with the date
  22 October
- **THEN** the server rejects the save, names the past date, and the topic keeps 25 October

### Requirement: What publishing still needs

Each topic that the server returns SHALL carry what publishing still needs, as the publish rule
names it: the missing fields of Title, Release date, Duration, Description, Setting, Interlocutor,
Role, Opening line, Translation and Steps, the numbers of the incomplete steps, and a past date. For
a draft, it SHALL also name a taken date when another topic of the same date is
published.

#### Scenario: A taken date

- **WHEN** a published topic has the date 24 October, and the admin app reads a draft of
  24 October with every field
- **THEN** the draft names the taken date and nothing else

#### Scenario: A ready draft

- **WHEN** the admin app reads a draft with every field and a free date of today or later
- **THEN** the draft names nothing

### Requirement: One published topic per date

A date SHALL have at most one published topic. Drafts MAY share a date with each other and with a
published topic. The server SHALL reject a publish or a save that puts a second published topic on
a date, and SHALL change nothing then.

#### Scenario: Two drafts on one date

- **WHEN** the owner saves two drafts with the date 24 October
- **THEN** the server stores both

#### Scenario: A second published topic

- **WHEN** a published topic has the date 24 October, and the owner publishes a draft with the date
  24 October
- **THEN** the server rejects the publish and the draft stays a draft

#### Scenario: A save onto a taken date

- **WHEN** published topics have the dates 24 and 25 October, and the owner saves the topic of
  25 October with the date 24 October
- **THEN** the server rejects the save and the topic keeps the date 25 October

#### Scenario: Two publishes at once

- **WHEN** two drafts have the date 24 October, and the owner publishes both at the same time
- **THEN** only one of them is published

### Requirement: A published topic goes out on its date

The server SHALL derive the status of a topic from the day in Moscow time: Draft for a draft;
Published for a published topic with a later date; Today for a published topic with today's date;
Out for a published topic with an earlier date. On its date, the published topic SHALL be the
topic of the day.

#### Scenario: The statuses

- **WHEN** today is 23 October in Moscow, and published topics have the dates 22, 23 and 24 October
- **THEN** their statuses are Out, Today and Published

#### Scenario: The day in Moscow

- **WHEN** the time is 22:30 UTC on 23 October, which is 01:30 on 24 October in Moscow, and a
  published topic has the date 24 October
- **THEN** its status is Today

#### Scenario: A draft on today's date

- **WHEN** a draft has today's date
- **THEN** its status is Draft

### Requirement: Saving a published topic

A save of a published topic SHALL keep it published. The server SHALL apply the publish rule to
it, and SHALL keep the stored topic when the rule rejects the save. A publish of a topic that is
already published SHALL act as its save.

#### Scenario: A save that empties a field

- **WHEN** the owner saves a published topic with an empty setting
- **THEN** the server rejects the save, names Setting, and the topic keeps its stored fields

#### Scenario: A save with every field

- **WHEN** the owner saves a published topic with a new title and every field
- **THEN** the server stores the title and the topic stays published

#### Scenario: Publish of a published topic

- **WHEN** the admin app publishes a topic that is already published, with a new title
- **THEN** the server applies the publish rule, stores the title, and the topic stays published

### Requirement: The calendar

The server SHALL derive the calendar. It SHALL have one row for each topic, and one row for each
day from today to the 13th day after today that has no topic. The rows SHALL be in date order, and
topics of one date in the order of their titles, ignoring case.

#### Scenario: The rows

- **WHEN** today is 23 October, a topic has the date 22 October and a draft has the date 25 October
- **THEN** the calendar has the row of 22 October, the empty days 23 and 24 October, the row of
  25 October, and the empty days from 26 October to 5 November

#### Scenario: No topics yet

- **WHEN** the server has no topics
- **THEN** the calendar has 14 empty days from today, and To do lists them

### Requirement: A topic row of the calendar

A topic row SHALL have the title, the interlocutor, the number of steps, the number of complete
steps and the status. The interlocutor SHALL be the name of the character, or a guest. A step SHALL
be complete when its task, Pass when and outcome are filled.

#### Scenario: The steps of a row

- **WHEN** a draft has five steps, and three of them have no outcome
- **THEN** its row has 5 steps and 2 complete steps

### Requirement: The To do of the calendar

The calendar SHALL have To do: each day from today to the 13th day after today without a topic,
and each draft with today's date or a later date whose date has no published topic, in date order.
A draft in To do SHALL name what publishing still needs.

#### Scenario: To do

- **WHEN** today is 23 October, a draft with the date 22 October misses Setting, and a draft with
  the date 25 October misses Steps
- **THEN** To do lists the empty days 23 and 24 October, the draft of 25 October with Steps, and
  the empty days from 26 October to 5 November, and does not list the draft of 22 October

#### Scenario: A draft on a date with a published topic

- **WHEN** a published topic and a draft have the date 24 October
- **THEN** To do does not list the draft, and All topics shows both

### Requirement: To do

To do SHALL be above All topics on Topics and SHALL list the To do of the calendar in the order of
the server. An empty day SHALL show its date in the error tone, a note that the day has no topic
and that users get an empty day, and Create. A draft SHALL show its date, its title, a note and
Open. Create SHALL open a new topic with the date of the day. Open SHALL open the Topic screen of
the draft. Without entries, To do SHALL say that there is nothing to do.

#### Scenario: An empty day

- **WHEN** To do has the empty day 26 October, and the owner clicks Create
- **THEN** the Topic screen shows a new topic, and Release date shows Sunday, 26 October

#### Scenario: Nothing to do

- **WHEN** the calendar has no To do
- **THEN** To do says that there is nothing to do

### Requirement: The note of a draft in To do

The note of a draft in To do SHALL say that it is a draft to publish when publishing needs nothing
more, and SHALL name what publishing still needs otherwise, with the numbers of the incomplete
steps.

#### Scenario: A draft that misses fields

- **WHEN** To do has a draft that misses Description, and whose steps 3, 4 and 5 are not complete
- **THEN** its note names the description and the steps 3–5, and Open opens the Topic screen of the
  draft

#### Scenario: A ready draft

- **WHEN** To do has a draft that publishing needs nothing more for
- **THEN** its note says that it is a draft to publish

### Requirement: Publish on the Topic screen

Publish SHALL be beside Save draft. For a saved draft, Publish SHALL first ask inside the screen,
with Publish and Cancel: the topic goes out on its date for all users, it cannot be unpublished,
and it can be edited. Publish in the question SHALL send the form, with an empty date and an empty
duration as no value. Cancel SHALL send nothing. The admin app SHALL show the topic that the server
returns. For a new topic, Publish SHALL be disabled.

#### Scenario: The question

- **WHEN** the owner clicks Publish on a saved draft of 24 October
- **THEN** a question inside the screen says that the topic goes out on 24 October for all users,
  that it cannot be unpublished and that it can be edited, with Publish and Cancel

#### Scenario: Cancel

- **WHEN** the question shows, and the owner clicks Cancel
- **THEN** the question closes, and the admin app sends nothing and keeps the form

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Publish
- **THEN** it keeps the form as it is and shows a snackbar that the publish failed

#### Scenario: A new topic

- **WHEN** the owner clicks New topic
- **THEN** Publish is disabled

### Requirement: The toolbar of a published topic

For a published topic, Publish SHALL be hidden, Save draft SHALL show Save, and the subtitle and the
status SHALL show its state.

#### Scenario: The publish succeeds

- **WHEN** the owner confirms the publish of a draft with every field
- **THEN** the subtitle shows the release date and that the topic is published, Publish is hidden,
  and Save draft shows Save

### Requirement: A rejected write on the Topic screen

A 400 or a 409 on Publish, on Save draft or on Save SHALL keep the form and show one error beside
the buttons in three parts: what happened, that the changes are not saved, and the next step. The
error of a 400 SHALL name what the server names. The error of a 409 SHALL say that the date already
has a published topic, and that the owner chooses another date.

#### Scenario: A rejected publish

- **WHEN** the server answers 400 on Publish and names Setting and the steps 3 and 5
- **THEN** the error says that the topic is not published, names the setting and the steps 3 and 5,
  says that the changes are not saved, and says to fill them and publish again

#### Scenario: The date has a published topic

- **WHEN** the server answers 409 on Publish or on Save
- **THEN** the error says that the date already has a published topic, that the changes are not
  saved, and to choose another date

#### Scenario: A rejected draft

- **WHEN** the server answers 400 on Save draft and names Title
- **THEN** the error says that the draft is not saved, names the title, and says to fill it and
  save again

### Requirement: Moving the date of a published topic

When the owner saves a published topic whose status is Published or Today with a release date
other than the stored one, the admin app SHALL first ask inside the screen: the stored date will be
left without a topic. The question SHALL have Save and Cancel. Cancel SHALL send nothing.

#### Scenario: A move

- **WHEN** a published topic of 24 October is upcoming, and the owner picks 26 October and clicks
  Save
- **THEN** a question says that 24 October will be left without a topic, with Save and Cancel

#### Scenario: The same date

- **WHEN** the owner saves a published topic with its stored date
- **THEN** the admin app sends the save with no question

#### Scenario: Cancel the move

- **WHEN** the question shows, and the owner clicks Cancel
- **THEN** the admin app sends nothing and keeps the form

### Requirement: The status of a publish

While the publish runs, the admin app SHALL show Publishing… beside the buttons, and SHALL disable
Save draft, Publish, the fields, the date popup, Add step and the back arrow. The error of a
rejected publish SHALL stay until the next click on Save draft or Publish.

#### Scenario: The publish runs

- **WHEN** the owner confirms Publish and the server has not answered yet
- **THEN** Publishing… shows beside the buttons, and Save draft, Publish, the fields, the date
  popup, Add step and the back arrow are disabled

#### Scenario: An edit after a rejected publish

- **WHEN** the server rejected the publish, and the owner edits a field
- **THEN** the error stays beside the buttons, says that it is about the version that the server
  saw, and stays until the next click on Save draft or Publish

### Requirement: The publish note

With no save or publish status, the status beside the buttons SHALL come from the last answer of
the server. For a new topic, it SHALL say to save a draft to publish it. For a draft, it SHALL say
that all fields are filled, or name what publishing still needs: the missing fields, the numbers of
the incomplete steps, a past date to change, or a taken date to change.

#### Scenario: A new topic

- **WHEN** the owner clicks New topic
- **THEN** the status says to save a draft to publish it

#### Scenario: A ready draft

- **WHEN** the owner opens a saved draft that publishing needs nothing more for
- **THEN** the status says that all fields are filled

#### Scenario: A draft that misses fields

- **WHEN** the owner opens a saved draft that misses Duration
- **THEN** the status names the duration as what publishing still needs

#### Scenario: A past date

- **WHEN** the owner opens a saved draft whose date is before today
- **THEN** the status says that the date has passed and to choose a new release date

#### Scenario: A taken date

- **WHEN** the owner opens a saved draft of 24 October, and 24 October has a published topic
- **THEN** the status says that 24 October already has a published topic and to choose another date

### Requirement: The note of a published topic

For a published topic, the note SHALL show the date it goes out; for a topic of today, that it
goes out today and that users see edits at once; for a topic that is out, the date it went out.
The words SHALL agree with the feminine gender of the Russian word for topic. After a field edit,
the note SHALL stay and SHALL say that it is about the version that the server saw.

#### Scenario: An upcoming topic

- **WHEN** the owner opens a published topic of 24 October, and today is 23 October
- **THEN** the status says that the topic goes out on 24 October

#### Scenario: A topic of today

- **WHEN** the owner opens a published topic of today
- **THEN** the status says that the topic goes out today and that users see edits at once

#### Scenario: An edit after an answer

- **WHEN** the status shows a note from the server, and the owner edits a field
- **THEN** the note stays and says that it is about the version that the server saw

## MODIFIED Requirements

### Requirement: A draft needs a title and a release date

A new topic SHALL be a draft. The server SHALL remove the spaces at the start and at the end of
each text field, and SHALL drop a step whose task, Pass when and outcome are all empty. Then it
SHALL reject a topic without a title, without a release date, with a duration that is not a whole
number of minutes greater than zero, or with a character that does not exist. An empty duration
SHALL be valid for a draft.

#### Scenario: A draft with a title and a date

- **WHEN** the owner saves a topic with only a release date and a title with spaces around it
- **THEN** the server stores the draft with the title without the spaces

#### Scenario: A rejected draft

- **WHEN** the owner saves a topic with a title of spaces only, without a release date, with a
  duration of 0 minutes, or with a character that does not exist
- **THEN** the server rejects the topic and stores nothing

#### Scenario: An empty step

- **WHEN** the owner saves a topic with a filled step and an empty step after it
- **THEN** the server stores only the filled step

### Requirement: The admin API serves topics

Behind the admin key, the server SHALL return the calendar at `GET /admin/topics/calendar`, return
one topic at `GET /admin/topics/{topicId}`, create a draft and return it with its identifier at
`POST /admin/topics`, replace the fields of a topic and return it at `PUT /admin/topics/{topicId}`,
and replace the fields of a topic, publish it and return it at
`POST /admin/topics/{topicId}/publish`. Each topic that the server returns SHALL carry its status
and what publishing still needs. A 400 for a topic that a rule rejects SHALL name what the rule
misses. The server SHALL answer 409 when a write puts a second published topic
on a date.

#### Scenario: No admin key

- **WHEN** a call to the topics API has no admin key or a wrong one
- **THEN** the server answers 401 and changes nothing

#### Scenario: An unknown topic

- **WHEN** the admin app reads, updates or publishes a topic that does not exist
- **THEN** the server answers 404

#### Scenario: A rejected topic

- **WHEN** the admin app sends a topic that the draft rule rejects, or a release date that is not a
  date
- **THEN** the server answers 400 and stores nothing

#### Scenario: A rejected publish

- **WHEN** the admin app publishes a topic without a description
- **THEN** the server answers 400, names Description, and stores nothing

#### Scenario: A taken date

- **WHEN** the admin app publishes a draft on a date that has a published topic
- **THEN** the server answers 409 and stores nothing

### Requirement: The Topics list

Topics SHALL show the subtitle One topic a day, the section To do, and the section All topics with
a table of the columns Date, Topic, Led by, Steps and Status: one row for each row of the calendar,
in the order of the server. Date SHALL show the short day of the week, the day and the month, such
as Fri, 24 Oct. Led by SHALL show the name of the character, Guest, or nothing without an
interlocutor. Steps SHALL show the number of steps when every step is complete, and the complete
steps of the steps otherwise, such as 2 of 5. Status SHALL show Draft in the warning tone,
Published and Today in the success tone, and Out in the muted tone. The row of an empty day SHALL
show a dash in Topic and Create in Status. A note below the table SHALL say that users get one
topic a day and that a published topic goes out on its date. A click on a title SHALL open the
Topic screen of the topic. Create SHALL open a new topic with the date of its row. The toolbar
SHALL have New topic.

#### Scenario: Topics with rows

- **WHEN** the calendar has a published topic for 24 October and an empty day 25 October
- **THEN** All topics shows the row Fri, 24 Oct with its title, its character, its steps and
  Published, and the row Sat, 25 Oct with a dash and Create, and a click on the title opens the
  Topic screen of that topic

#### Scenario: Create on an empty day

- **WHEN** the owner clicks Create in the row of 25 October
- **THEN** the Topic screen shows a new topic, and Release date shows Saturday, 25 October

#### Scenario: Incomplete steps

- **WHEN** a row has 5 steps and 2 complete steps
- **THEN** Steps shows 2 of 5

#### Scenario: A return to Topics

- **WHEN** the owner returns to Topics from another screen
- **THEN** the admin app shows the calendar that it has at once, and replaces it with the calendar
  from the server when the server answers

#### Scenario: The calendar does not update

- **WHEN** the admin app shows a calendar and cannot load it again
- **THEN** it keeps the calendar, and shows above it an error that the topics did not update, with
  Retry

#### Scenario: Retry over a kept calendar

- **WHEN** the owner clicks Retry in the error above a kept calendar
- **THEN** the error stays with a progress mark and a disabled Retry until the server answers

#### Scenario: No topics yet

- **WHEN** the server has no topics
- **THEN** All topics shows the 14 empty days from today, each with Create, and New topic stays in
  the toolbar

#### Scenario: The list does not load

- **WHEN** the admin app has no calendar yet and cannot load it
- **THEN** Topics shows an error that the topics did not load, and Retry

### Requirement: The Topic screen

The Topic screen SHALL have the numbered sections Scene, Interlocutor, Opening line and Steps.
Scene SHALL have Title, Release date, Duration in minutes, Description and Setting. Release date
SHALL be a popup with the day of the week, the day and the month, which opens a date picker.
Duration SHALL take at most three digits. Opening line SHALL have English and Translation. The
toolbar SHALL have a back arrow to Topics, the status, Save draft and Publish.

#### Scenario: A saved topic

- **WHEN** the owner opens a saved topic
- **THEN** the title is the title of the topic, the subtitle shows its release date and that it is
  a draft or published, and the fields hold the topic

#### Scenario: A new topic

- **WHEN** the owner clicks New topic
- **THEN** the title is New topic, the subtitle says that it is not saved yet, the fields are empty,
  Release date shows Choose a date, the Steps section has one empty step, and a hint says that a
  draft needs a title and a release date and that the rest can wait

#### Scenario: Pick a date

- **WHEN** the owner picks a day in the date picker of Release date
- **THEN** the popup shows that day with its day of the week, such as Friday, 24 October

#### Scenario: Duration

- **WHEN** the owner types a fourth digit or a letter into Duration
- **THEN** the field does not take it

#### Scenario: The topic does not load

- **WHEN** the admin app cannot load the topic or the cast
- **THEN** the screen shows an error that the topic did not load, and Retry

### Requirement: The status of a save

While the save runs, the admin app SHALL show Saving… beside Save draft, and SHALL disable Save
draft, Publish, the fields, the date popup, Add step and the back arrow. A field edit SHALL remove
the Saved status. The error of a rejected save SHALL stay until the next click on Save draft or
Publish, and after a field edit SHALL say that it is about the version that the server saw.

#### Scenario: The save runs

- **WHEN** the owner clicks Save draft and the server has not answered yet
- **THEN** Saving… shows beside Save draft, and Save draft, Publish, the fields, the date popup, Add
  step and the back arrow are disabled

#### Scenario: An edit after a rejected draft

- **WHEN** the server rejected the draft, and the owner edits a field
- **THEN** the error stays beside Save draft, says that it is about the version that the server saw,
  and stays until the next click on Save draft or Publish

#### Scenario: An edit after a save

- **WHEN** the Saved status shows, and the owner edits a field
- **THEN** the Saved status goes away

#### Scenario: A rejected save of a published topic

- **WHEN** the server answers 400 on Save of a published topic and names Setting
- **THEN** the admin app keeps the form, and the error names the setting and says that the changes
  are not saved

### Requirement: The Interlocutor and Steps sections

Interlocutor SHALL offer one radio row for each character of the cast, in the order of the server,
with the topic count of the character on the right, and then Guest with the note Only for this
topic. The guest description field SHALL show only when Guest is chosen. Role in the scene SHALL be
below the choice. Steps SHALL show one row for each step with its number, Task on the screen, Pass
when and Step outcome, then Add step, and a note that the steps and the check are the same for
every rating level.

#### Scenario: Guest

- **WHEN** the owner chooses Guest
- **THEN** the guest description field shows below the choice

#### Scenario: Add step

- **WHEN** the owner clicks Add step
- **THEN** the Steps section gets an empty step below the last one

#### Scenario: A step edit

- **WHEN** the owner edits a field of the second of two steps
- **THEN** only the second step changes

#### Scenario: The topic counts

- **WHEN** Sam leads 12 topics and Mia leads 1 topic
- **THEN** the row of Sam shows 12 topics and the row of Mia shows 1 topic, each in the Russian form
  for its number

### Requirement: Saving a draft

Save draft SHALL send the topic to the server, with an empty release date and an empty duration as
no value. When the server accepts it, the admin app SHALL show the topic that the server returns,
and a Saved status beside Save draft. A new topic SHALL then become a saved topic.

#### Scenario: The save succeeds

- **WHEN** the owner fills the title and the release date of a new topic and clicks Save draft
- **THEN** the title becomes the title of the topic, the subtitle shows the release date, and the
  Saved status shows beside Save draft

#### Scenario: The server rejects the draft

- **WHEN** the server answers 400 on Save draft and names the duration
- **THEN** the admin app keeps the form and shows one error beside Save draft that names the
  duration, says that the changes are not saved, and says to fix it and save again

#### Scenario: The server does not answer

- **WHEN** the admin app cannot reach the server on Save draft
- **THEN** it keeps the form as it is and shows a snackbar that the save failed
