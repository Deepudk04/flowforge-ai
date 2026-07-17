alter table generation_jobs
    add column updated_at timestamp with time zone;

update generation_jobs
set updated_at = created_at
where updated_at is null;

alter table generation_jobs
    alter column updated_at set not null;

alter table generation_jobs
    add column failure_message text;

alter table generated_documents
    add column updated_at timestamp with time zone;

update generated_documents
set updated_at = created_at
where updated_at is null;

alter table generated_documents
    alter column updated_at set not null;

alter table workflow_diagrams
    add column updated_at timestamp with time zone;

update workflow_diagrams
set updated_at = created_at
where updated_at is null;

alter table workflow_diagrams
    alter column updated_at set not null;

alter table workflow_diagrams
    add column warnings text;
