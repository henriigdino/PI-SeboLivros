-- Dados iniciais criados automaticamente na primeira execução de cada máquina.
-- O INSERT só acontece se o registro não existir, para não sobrescrever
-- alterações feitas depois pelo usuário.

-- Usuário administrador: admin@sebo.com / admin123 (senha em BCrypt)
INSERT INTO usuarios (nome, email, senha, tipo, ativo)
SELECT 'Administrador', 'admin@sebo.com',
       '$2a$10$P0xk4U9cgwcSqFDQCPyGAe330enlq58mKmHgmpFAk7fX.XVVjUTwm',
       'ADMIN', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'admin@sebo.com'
);

-- Exemplo de fornecedor para o time ter dados de teste
INSERT INTO fornecedores (nome, documento, email, telefone, endereco, ativo)
SELECT 'Fornecedor Exemplo', '00000000000191', 'contato@exemplo.com',
       '11999999999', 'Rua Exemplo, 1', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM fornecedores WHERE documento = '00000000000191'
);
