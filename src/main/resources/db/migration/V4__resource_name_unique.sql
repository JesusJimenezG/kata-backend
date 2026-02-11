-- V4: Ensure unique resource names (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_resource_name'
          AND conrelid = 'resource'::regclass
    ) THEN
        ALTER TABLE resource
            ADD CONSTRAINT uq_resource_name UNIQUE (name);
    END IF;
END $$;
