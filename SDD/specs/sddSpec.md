# Spec Driven Development

## Specification

Spec Driven Development (SDD) is the documentation discipline used in Cantinho
das Lavandas to define what will be built before deciding how it will be
implemented.

The process has five document layers:

- specs define the product concept and expected behavior;
- plans define the technical implementation strategy;
- tasks define small executable work items;
- implementation files define execution order and operating rules;
- implementation reports record what happened during execution.

The goal is to keep product intent explicit, technical decisions traceable and
code aligned with agreed behavior.

## Scope

This process applies to modules, systems, features, integrations and relevant
operational concepts in Cantinho das Lavandas.

Before executing a task, an agent must read every required spec and its
prerequisites. Product changes begin in the spec, then propagate to plans and
tasks. Implementation must not silently redefine the product.

Existing documentation outside the SDD folders remains valid reference material.
When it becomes authoritative for a new task, the relevant decisions must be
captured or explicitly referenced by that task's spec and plan.

## Capabilities

### Define Specs

Specs describe what a product, module, system or feature is and what it must do,
without depending on code knowledge.

Every spec contains, in this order:

1. `Specification` — definition of the concept;
2. `Scope` — relationship with the project and operating boundaries;
3. `Capabilities` — expected behavior in product language;
4. `Prerequisite Specs` — documents that must be understood first;
5. `Spec Degree` — distance from the project mother spec.

### Define Prerequisite Specs

Every product spec declares its prerequisites by path or stable spec name. A
spec with no prerequisites says `None` explicitly.

Every product spec must have a direct or indirect path to the project mother
spec. Isolated specs have an invalid genealogy.

Before defining a new spec's prerequisites, hierarchy or degree, evaluate which
existing specs must be understood to implement and operate the proposed
capability correctly. This evaluation must consider, when applicable:

- governing product behavior and broader operational rules;
- module and system architecture;
- privacy, security, audit and authorization obligations;
- related lifecycle, retention and historical-data behavior;
- integration, persistence and cross-module constraints.

Every spec identified as required knowledge must be declared as a prerequisite,
either directly or through a clearly traceable prerequisite chain. Do not omit
a required prerequisite merely to keep the new spec closer to the project
mother spec.

Determine the hierarchy and `Spec Degree` only after this knowledge assessment
and prerequisite selection. The desired degree must not be chosen first and
then used to manufacture, omit or rearrange prerequisites. Include only specs
whose knowledge is genuinely necessary; degree inflation through unrelated
prerequisites is also invalid.

### Define The Main Spec

Every project has one mother spec, named with this pattern:

```text
projectNameMainSpec
```

For this project, the mother spec is `cantinhoDasLavandasMainSpec`. It is the
root of the product knowledge tree and has degree 0. This SDD process spec does
not participate in the product genealogy.

### Define Spec Degrees

- the mother spec has degree 0;
- a direct child of the mother spec has degree 1;
- any other spec has a degree one greater than the highest degree among its
  product prerequisites;
- every product spec degree must be traceable to the mother spec.

#### Restrict The Creation Of Degree-One Specs

Degree-one specs are mother specs for major branches of the product knowledge
tree. They must not be created merely because a new module, feature, workflow
or integration needs documentation.

Creating a degree-one spec requires an explicit and exceptionally specific
user instruction that:

1. states that the new spec must be a direct child of the project mother spec;
2. expressly authorizes degree `1` for that spec;
3. identifies the major product-knowledge branch that the spec will own.

An instruction to create a spec, plan a feature, document a module or organize
an implementation does not implicitly authorize a degree-one spec. The agent
must not infer this authorization from the breadth, importance, name or
cross-cutting nature of the subject.

Without all three explicit conditions above, every newly created product spec
must have degree greater than `1`. It must declare at least one valid degree-one
or deeper product prerequisite so that its computed degree is `2` or higher.
When no suitable prerequisite exists, the agent must stop and request a
specific genealogy decision instead of creating a new degree-one branch.

### Define Plans

Plans translate specs into technical direction. They may cover architecture,
module boundaries, integrations, persistence, contracts, migrations and tests.
Plans point to their governing specs and do not redefine product behavior.

When a plan cites code, it includes architecture context:

```text
FileName.java (architecture layer; owning class when applicable)
ClassName (architecture layer; class)
methodName (architecture layer; owning class)
```

Example:

```text
PublicBookingService (application/service; class)
PublicBookingUseCase (application/port/in; interface)
createBooking (application/service; PublicBookingService)
PublicBookingController.java (adapter/in/rest; PublicBookingController)
```

### Define Tasks

Tasks are independently executable work units. Each task lists all specs, plans
and implementation files required before execution, defines a bounded scope and
contains objective acceptance criteria.

Tasks are numbered sequentially inside their own implementation area and
suffixed by that area:

- `b`: backend;
- `f`: frontend;
- `a`: design and assets.

Backend, frontend and asset work remain in separate task files unless a newer
spec explicitly changes this rule.

Each area has an independent numeric sequence beginning at `001`. Therefore,
backend numbering does not advance or reserve frontend numbers, frontend
numbering does not advance or reserve backend numbers, and the same isolation
applies to design and asset tasks. For example, `001b` and `001f` are distinct,
valid tasks and may coexist.

#### Mark Completed Tasks In Their Names

When an SDD task has been fully executed, its acceptance criteria have been
verified, its prerequisite review has passed and its implementation report has
been created, add `DONE` to the task title and filename. Use this pattern:

```text
task-id-DONE-short-title.md
# Task task-id DONE — Short Title
```

For example:

```text
001b-DONE-login-failure-state.md
# Task 001b DONE — Persist Login Failure State And Policy
```

Update every SDD reference to the renamed task file as part of task completion.
Do not add `DONE` when a task is merely approved, ordered, started, partially
implemented, awaiting verification or blocked. The task's `Status` section and
implementation report remain mandatory; `DONE` is a visual completion marker,
not their replacement.

### Define Implementation Files

Operational files live in `SDD/implementation/`:

- `task-bootstrap.md` defines the rules for starting and completing a task;
- `implementation-order.md` defines the active ordered task sequence.

Before changing code for an SDD task, the agent must:

1. read the bootstrap;
2. read the ordered implementation file when executing a sequence;
3. read the current task;
4. read all specs and plans required by the task, including prerequisite specs;
5. implement only the current scope unless a dependency is strictly necessary;
6. preserve existing project architecture and naming patterns;
7. document any small decision required because the MVP is underspecified.

### Review Prerequisites After Each Task

At the end of each task, compare the result against:

- every required spec and all their prerequisite specs;
- every required plan;
- the acceptance criteria;
- the active implementation rules.

If a contradiction exists, stop the sequence, resolve it without violating the
documents and repeat the review. If resolution requires changing product intent,
update spec, plan and task in that order before implementation continues.

### Create Implementation Reports

Every executed SDD task produces a report under `SDD/ImplementationReport/` named:

```text
YYYY-MM-DD-task-id-short-title.md
```

The report includes:

- task id and implementation file executed;
- specs, prerequisite specs and plans read;
- a mandatory, explicit list of files created; when no file was created, the
  report states `None`;
- files changed;
- flows implemented;
- technical and MVP decisions;
- difficulties, problems and resolutions;
- tests and verification commands, including anything not run and why;
- prerequisite review results, contradictions found, fixes applied and final
  confirmation of conformity.

No fix is valid if it conflicts with a required spec, plan or acceptance
criterion. Such a conflict must be documented and remain blocked until the SDD
documents are changed in the correct order.

### Keep Layers Separate

Specs state what the product is and does. Plans state how it will be built.
Tasks state the next bounded work. Implementation files control execution.
Reports record what actually happened.

### Update Documents In Order

When a product assumption changes, update:

1. spec;
2. plan;
3. task;
4. implementation;
5. report.

## Prerequisite Specs

None.

## Spec Degree

Process spec. Not part of the product genealogy.
