create table generation_jobs (
    id varchar(80) primary key,
    status varchar(40) not null,
    resource_type varchar(40) not null,
    resource_id varchar(80),
    created_at timestamp with time zone not null
);

create table generated_documents (
    id varchar(80) primary key,
    title varchar(160) not null,
    document_type varchar(80) not null,
    content text not null,
    created_at timestamp with time zone not null
);

create table workflow_diagrams (
    id varchar(80) primary key,
    title varchar(160) not null,
    mermaid text not null,
    created_at timestamp with time zone not null
);

create index idx_generation_jobs_status on generation_jobs(status);
create index idx_generated_documents_type on generated_documents(document_type);
create index idx_workflow_diagrams_created_at on workflow_diagrams(created_at);