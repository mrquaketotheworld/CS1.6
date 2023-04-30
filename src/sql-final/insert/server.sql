insert into server (server_id, server)
values ('abc', 'Nanax') on conflict do nothing;

select * from map_server where server_id = 'abc';
-- if null ->
-- insert default maps

insert into map_server (map, server_id, is_main)
values
('Nuke', 'abc', true),
('Mirage', 'abc', true),
('Dust2', 'abc', true),
('Tuscan', 'abc', true),
('Train', 'abc', true),
('Cache', 'abc', true),
('Inferno', 'abc', true);

insert into map_server (map, server_id, is_fun)
values
('Assault', 'abc', true),
('HEKrystal', 'abc', true),
('IceWorld', 'abc', true),
('Mansion', 'abc', true),
('PoolDay', 'abc', true),
('PoolDay2', 'abc', true);

insert into map_server (map, server_id, is_extra)
values
('Aztec', 'abc', true),
('Cbble', 'abc', true),
('CPLFire', 'abc', true),
('CPLMill', 'abc', true),
('Dust', 'abc', true),
('Overpass', 'abc', true),
('Vertigo', 'abc', true),
('Prodigy', 'abc', true);

select * from map_server where server_id = 'abc' and is_main = true;
