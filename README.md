1. ollama installeren (Want je wilt virtualisatie laag niet op je LLM van docker)
2. profile switch
3. Monitoring omgevingen op te zetten met docker compose file in /monitoring (ELK of Grafana)
   - Voor ELK: `docker-compose -f monitoring/docker-compose-elk-monitoring.yml up -d` en gebruik profile `elk-monitoring`
   - Voor Grafana: `docker-compose -f monitoring/docker-compose-grafana-monitoring.yml up -d` en gebruik profile `grafana-monitoring`


