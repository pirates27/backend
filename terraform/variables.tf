variable "aws_region" {
  type        = string
  description = "AWS region to deploy resources"
  default     = "ap-south-1"
}

variable "project_name" {
  type        = string
  description = "Name of the project"
  default     = "landlens"
}

variable "environment" {
  type        = string
  description = "Target deployment environment"
  default     = "production"
}

variable "container_port" {
  type        = number
  description = "Port exposed by the Spring Boot Docker container"
  default     = 8080
}

variable "fargate_cpu" {
  type        = number
  description = "Fargate CPU units (256 = 0.25 vCPU)"
  default     = 512
}

variable "fargate_memory" {
  type        = number
  description = "Fargate Memory in MB"
  default     = 1024
}

variable "ecs_desired_count" {
  type        = number
  description = "Desired number of ECS tasks running concurrently"
  default     = 2
}
