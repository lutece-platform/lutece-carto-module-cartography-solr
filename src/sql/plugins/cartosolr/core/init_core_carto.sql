DELETE FROM core_admin_right WHERE id_right = 'CARTO_EXPORT_SOLR';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('CARTO_EXPORT_SOLR','module.cartography.solr.export.name',1,'jsp/admin/plugins/cartosolr/ManageExportDataLayer.jsp','module.cartography.solr.export.description',0,'carto',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'CARTO_EXPORT_SOLR';
INSERT INTO core_user_right (id_right,id_user) VALUES ('CARTO_EXPORT_SOLR',1);