## ADDED Requirements

### Requirement: A conversation starts with the opening line

The server SHALL start a conversation of a topic at a rating level with the opening line of the
topic as the first line of the interlocutor, in English with its Russian translation. Step 1 SHALL
be the current step. The start SHALL call no model.

#### Scenario: The start

- **WHEN** a conversation of a topic with three steps starts at B1
- **THEN** it holds the opening line and its translation, and step 1 is current

### Requirement: A turn checks the answer, then the character replies

A turn SHALL take a conversation and the answer of the learner, without the spaces at its start and
its end. It SHALL first run the step check of the answer against the current step, and then the
character reply, which gets the verdict of the check. The turn SHALL add the answer, its verdict
and the reply to the conversation.

#### Scenario: A turn

- **WHEN** the learner answers on step 1 and the check returns `continue`
- **THEN** the conversation holds the answer with `continue`, and then the reply of the
  interlocutor in English with its Russian translation

### Requirement: The step check returns one of four verdicts

The step check SHALL send the Step check prompt with its variables filled. The verdict SHALL be
`accepted`, `continue`, `unclear` or `not_accepted`. The step check SHALL NOT depend on the level:
two levels SHALL get the same prompt for the same answer.

#### Scenario: The check prompt

- **WHEN** the learner answers on a step with a task and Pass when
- **THEN** the check prompt holds the task, Pass when, the last line of the interlocutor and the
  answers of the learner on this step, the newest last

#### Scenario: Two levels

- **WHEN** the same answer on the same step is checked at A2 and at C1
- **THEN** both checks get the same prompt

### Requirement: Only an accepted answer completes the step

`accepted` SHALL complete the current step, and the next step SHALL become current. When the last
step is accepted, the conversation SHALL be finished. `continue`, `unclear` and `not_accepted`
SHALL keep the current step. The answers of the step SHALL stay in the next check of the same step.

#### Scenario: Accepted

- **WHEN** the check returns `accepted` on step 1 of three
- **THEN** step 2 is current

#### Scenario: Continue

- **WHEN** the check returns `continue` on step 1
- **THEN** step 1 stays current, and the next check of step 1 gets both answers

#### Scenario: Unclear

- **WHEN** the check returns `unclear` on step 1
- **THEN** step 1 stays current

#### Scenario: Not accepted

- **WHEN** the check returns `not_accepted` on step 1
- **THEN** step 1 stays current

#### Scenario: The last step

- **WHEN** the check returns `accepted` on the last step
- **THEN** the conversation is finished, and the character still replies

### Requirement: The reply follows the verdict

The character reply SHALL send the Character reply prompt with its variables filled, and the
dialogue after it, oldest first: the lines of the interlocutor as the assistant, the answers of the
learner as the user, and the new answer last. `{step.n}` and `{step.task}` SHALL be the step that
the answer was checked against, and `{check.verdict}` SHALL be its verdict, so the reply does not
act as if a step is done when it is not.

#### Scenario: The reply prompt after a not accepted answer

- **WHEN** the check returns `not_accepted` on step 2
- **THEN** the reply prompt holds the number 2, the task of step 2 and `not_accepted`

#### Scenario: The dialogue

- **WHEN** the learner answers for the second time
- **THEN** the reply gets the opening line, the first answer, the first reply and the second answer,
  in this order

### Requirement: The server fills the variables of the prompts

The server SHALL replace each variable of a prompt with its value as plain text. Text in braces
that is not a variable of that prompt SHALL stay as it is.

#### Scenario: Text in braces that is not a variable

- **WHEN** the reply prompt holds `{answers}`, which is not a variable of the reply prompt
- **THEN** `{answers}` stays as it is in the reply prompt

### Requirement: The interlocutor in the prompt

`{interlocutor}` SHALL hold three lines with the name, the personality and the manner of speech for
a character of the cast, and the guest description of the topic for a guest.

#### Scenario: A character of the cast

- **WHEN** the interlocutor is the character Sam
- **THEN** `{interlocutor}` holds the name, the personality and the manner of speech of Sam

#### Scenario: A guest

- **WHEN** the interlocutor is a guest
- **THEN** `{interlocutor}` holds the guest description of the topic

### Requirement: The values of the scene and the step

`{topic.setting}` and `{topic.role}` SHALL be the setting and the role of the topic.
`{level.rules}` SHALL be the saved block of the level of the conversation. `{step.n}` SHALL be the
number of the checked step, from 1, and `{step.task}` and `{step.passWhen}` its task and Pass when.
`{last.line}` SHALL be the last English line of the interlocutor before the answer. `{answers}`
SHALL be the answers of the learner on the current step, one per line, the newest last.

#### Scenario: The second answer on a step

- **WHEN** the learner answers `Hi` and then `I am Lyosha` on step 1, after the opening line
- **THEN** `{answers}` is `Hi` and `I am Lyosha` on two lines, and `{last.line}` is the reply to `Hi`

### Requirement: A changed level block shows in the next reply

The reply SHALL read the block of the level at each turn, so a saved change applies to the next
reply of that level.

#### Scenario: A changed block

- **WHEN** the owner saves a new block for B1 between two turns at B1
- **THEN** the second reply prompt holds the new block, and the first held the old one

### Requirement: Each call uses the model of its task

The step check SHALL use the model of Step check. The character reply SHALL use the model of
Character reply for the level of the conversation, as the task models say. The server SHALL read
the models at each turn.

#### Scenario: A level with a model

- **WHEN** B1 has the model `deepseek-v4-pro` and both tasks have `deepseek-flash`
- **THEN** a turn at B1 checks with `deepseek-flash` and replies with `deepseek-v4-pro`

### Requirement: The server sets the answer format of each call

The server SHALL add the answer format to the end of each prompt it sends. The step check SHALL ask
for JSON with `verdict`. The reply SHALL ask for JSON with `line`, the English line, and
`translation`, its Russian translation. The server SHALL read only that format: a verdict out of
the four, or a line or a translation that is missing or empty after the spaces are removed, SHALL
count as a failed call.

#### Scenario: An unknown verdict

- **WHEN** the step check answers with the verdict `maybe`
- **THEN** the turn fails and the conversation stays as it was

#### Scenario: A reply without a translation

- **WHEN** the reply answers with a line and an empty translation
- **THEN** the turn fails and the conversation stays as it was

### Requirement: A failed call changes nothing

When the step check or the reply fails, times out or answers in another format, the turn SHALL
fail, and the conversation SHALL stay as it was before the answer. The answer SHALL NOT be stored
with a verdict, and the step SHALL NOT change. When the step check fails, the server SHALL NOT call
the reply.

#### Scenario: The check fails

- **WHEN** the model of the step check fails
- **THEN** the turn fails, the reply is not called, and the conversation stays as it was

#### Scenario: The reply fails after an accepted answer

- **WHEN** the check returns `accepted` and the model of the reply fails
- **THEN** the turn fails, and the current step and the lines stay as they were

### Requirement: A turn that cannot run

The server SHALL NOT call a model and SHALL keep the conversation as it was when the answer is
empty, when the conversation is finished, when the topic does not exist, when the topic has no
interlocutor, when the topic no longer has the current step, when the current step has no task or
no Pass when, when the Step check or the
Character reply prompt has no saved text, or when the level has no saved block. The outcome SHALL
name the reason.

#### Scenario: An empty answer

- **WHEN** the learner sends an answer of spaces only
- **THEN** the turn does not run and names an empty answer

#### Scenario: A finished conversation

- **WHEN** the learner answers after the last step is accepted
- **THEN** the turn does not run and names a finished conversation

#### Scenario: A step without Pass when

- **WHEN** the current step of a draft has no Pass when
- **THEN** the turn does not run and names the incomplete step

#### Scenario: A prompt without a saved text

- **WHEN** the owner never saved the Character reply prompt
- **THEN** the turn does not run and names the Character reply prompt

#### Scenario: A level without a block

- **WHEN** the owner did not save the levels yet
- **THEN** the turn does not run and names the level
