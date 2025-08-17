docker exec -it timescaledb psql -U postgres -d docs_ops
select * from ops where doc_id='doc-1' and user_id='user-3';