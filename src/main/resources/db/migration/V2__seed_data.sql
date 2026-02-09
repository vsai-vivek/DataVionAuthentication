-- Insert default tenant
INSERT INTO tenants (name, domain, active) VALUES ('Default', 'default.com', true);

-- Insert default permissions
INSERT INTO permissions (resource, action, description) VALUES
('users', 'CREATE', 'Create new users'),
('users', 'READ', 'View user information'),
('users', 'UPDATE', 'Update user information'),
('users', 'DELETE', 'Delete users'),
('roles', 'CREATE', 'Create new roles'),
('roles', 'READ', 'View roles'),
('roles', 'UPDATE', 'Update roles'),
('roles', 'DELETE', 'Delete roles'),
('permissions', 'CREATE', 'Create new permissions'),
('permissions', 'READ', 'View permissions'),
('permissions', 'UPDATE', 'Update permissions'),
('permissions', 'DELETE', 'Delete permissions'),
('audit-logs', 'READ', 'View audit logs'),
('tenants', 'CREATE', 'Create new tenants'),
('tenants', 'READ', 'View tenants'),
('tenants', 'UPDATE', 'Update tenants'),
('tenants', 'DELETE', 'Delete tenants');

-- Insert default roles
INSERT INTO roles (name, description, tenant_id) VALUES
('SUPER_ADMIN', 'Super administrator with full access', 1),
('ADMIN', 'Administrator with management access', 1),
('USER', 'Regular user with basic access', 1);

-- Assign all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Assign user management permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE resource IN ('users', 'roles', 'audit-logs') AND action IN ('READ', 'UPDATE');

-- Assign basic permissions to USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE resource = 'users' AND action = 'READ';

-- Create default admin user (password: Admin@123)
-- BCrypt hash for "Admin@123" with strength 12
INSERT INTO users (username, email, password_hash, email_verified, source, tenant_id)
VALUES ('admin', 'admin@dauth.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIr8nQq5yy', true, 'LOCAL', 1);

-- Assign SUPER_ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
