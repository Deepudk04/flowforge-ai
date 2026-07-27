create table organizations (
    id varchar(80) primary key,
    name varchar(160) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table workspaces (
    id varchar(80) primary key,
    organization_id varchar(80) not null references organizations(id),
    name varchar(160) not null,
    description text,
    status varchar(40) not null,
    created_by varchar(160) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint chk_workspaces_status check (status in ('ACTIVE', 'ARCHIVED'))
);

create table source_documents (
    id varchar(80) primary key,
    organization_id varchar(80) not null references organizations(id),
    workspace_id varchar(80) not null references workspaces(id),
    title varchar(200) not null,
    original_filename varchar(255) not null,
    source_type varchar(40) not null,
    content_type varchar(120) not null,
    storage_path varchar(500) not null,
    extracted_content text not null,
    content_hash char(64) not null,
    size_bytes bigint not null,
    status varchar(40) not null,
    approved_by varchar(160),
    approved_at timestamp with time zone,
    rejection_reason text,
    failure_message text,
    created_by varchar(160) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint chk_source_documents_type check (source_type in ('TEXT', 'MARKDOWN', 'PDF')),
    constraint chk_source_documents_status check (status in ('UPLOADED', 'APPROVED', 'REJECTED', 'FAILED')),
    constraint uq_source_documents_workspace_hash unique (organization_id, workspace_id, content_hash)
);

create index idx_workspaces_organization on workspaces(organization_id, created_at, id);
create index idx_source_documents_workspace on source_documents(organization_id, workspace_id, created_at, id);
create index idx_source_documents_approved on source_documents(organization_id, workspace_id, status);

insert into organizations (id, name, created_at, updated_at)
values ('org_demo', 'FlowForge Synthetic Demo', current_timestamp, current_timestamp);
