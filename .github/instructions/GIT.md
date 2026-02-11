# Git workflow rules for AI agents

## Workflow

- Simple workflow: always commit directly to `main` (no feature branches).
- Create commits after a track/task is completed (avoid “half-done” commits).
- When the prompt is "run git commands", ask for a quick description of AI usage for the task being committed. If no entry is needed, the following prompt is "no entry" and continue with the git workflow.

## Best practices

- Keep commits small, focused, and scoped to a single concern.
- Do not introduce unrelated refactors in the same change set unless explicitly requested.
- Ensure the working tree is clean before committing (no accidental files).
- Prefer meaningful commit messages that describe the outcome (e.g., "Add reservation validation", "Fix inventory lookup").
