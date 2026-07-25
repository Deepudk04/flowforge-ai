alter table generation_jobs
    add constraint chk_generation_jobs_status
        check (status in (
            'PENDING',
            'RUNNING',
            'RETRY_SCHEDULED',
            'REVIEW_REQUIRED',
            'COMPLETED',
            'FAILED',
            'CANCELLED'
        ));
