-- V3: Role-based resource type permissions
-- Maps which resource types each role can access (reserve & view)

-- ============================================================
-- 1. Permission mapping table
-- ============================================================
CREATE TABLE role_resource_type_permission (
    role_id          INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    resource_type_id INTEGER NOT NULL REFERENCES resource_type(id) ON DELETE CASCADE,

    PRIMARY KEY (role_id, resource_type_id)
);

-- ============================================================
-- 2. Populate permissions per role
-- ============================================================

-- USER -> ROOM only
INSERT INTO role_resource_type_permission (role_id, resource_type_id)
SELECT r.id, rt.id FROM role r, resource_type rt
WHERE r.name = 'USER' AND rt.name = 'ROOM';

-- EMPLOYEE -> ROOM, CONFERENCE_ROOM, SHARED_TECH_EQUIPMENT, BILL_COUNTING_MACHINE
INSERT INTO role_resource_type_permission (role_id, resource_type_id)
SELECT r.id, rt.id FROM role r, resource_type rt
WHERE r.name = 'EMPLOYEE'
  AND rt.name IN ('ROOM', 'CONFERENCE_ROOM', 'SHARED_TECH_EQUIPMENT', 'BILL_COUNTING_MACHINE');

-- MANAGER -> all EMPLOYEE types + VIP_ROOM
INSERT INTO role_resource_type_permission (role_id, resource_type_id)
SELECT r.id, rt.id FROM role r, resource_type rt
WHERE r.name = 'MANAGER'
  AND rt.name IN ('ROOM', 'CONFERENCE_ROOM', 'VIP_ROOM', 'SHARED_TECH_EQUIPMENT', 'BILL_COUNTING_MACHINE');

-- HEAD_OF_OPERATIONS -> all MANAGER types + CORPORATE_VEHICLE (= everything)
INSERT INTO role_resource_type_permission (role_id, resource_type_id)
SELECT r.id, rt.id FROM role r, resource_type rt
WHERE r.name = 'HEAD_OF_OPERATIONS';

-- ADMIN -> everything
INSERT INTO role_resource_type_permission (role_id, resource_type_id)
SELECT r.id, rt.id FROM role r, resource_type rt
WHERE r.name = 'ADMIN';

-- ============================================================
-- 3. DB view: per-user allowed resource types (union of roles)
-- ============================================================
CREATE VIEW v_user_allowed_resource_types AS
SELECT DISTINCT ur.user_id, rtp.resource_type_id
FROM user_role ur
JOIN role_resource_type_permission rtp ON ur.role_id = rtp.role_id;
