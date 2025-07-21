#!/bin/bash
# Script para configurar RabbitMQ cluster
rabbitmq-server -detached
sleep 10
rabbitmqctl stop_app
rabbitmqctl join_cluster rabbit@rabbitmq1
rabbitmqctl start_app
rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all"}'
