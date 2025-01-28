# ECS160 HW1

[![Java CI with Maven](https://github.com/UtkarshNandy/ECS160_HW1/actions/workflows/maven.yml/badge.svg)](https://github.com/UtkarshNandy/ECS160_HW1/actions/workflows/maven.yml)

## Authors
- Denil Neil
- Utkarsh Nandy

Social media post analyzer implementing basic statistics calculations and database storage using Redis. 
The program analyzes posts and replies from Bluesky, calculating:
- Total number of posts (weighted and unweighted)
- Average number of replies per post
- Average interval between comments in HH:MM:SS format

Parent-child relationships between posts and replies are maintained in the Redis database.