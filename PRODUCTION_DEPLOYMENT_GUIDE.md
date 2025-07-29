# 🚀 SfaAI Production Deployment Guide

## 📋 Pre-Deployment Checklist

### 🔐 **Security Requirements (CRITICAL)**

- [ ] **Environment Variables Set**
  - [ ] `DB_URL` - Production database URL
  - [ ] `DB_USERNAME` - Production database username
  - [ ] `DB_PASSWORD` - Production database password
  - [ ] `JWT_SECRET` - Strong random secret (256+ bits)
  - [ ] `VAPI_API_KEY` - Your Vapi API key
  - [ ] `ELEVENLABS_API_KEY` - Your ElevenLabs API key
  - [ ] `WEBHOOK_SECRET` - Webhook verification secret
  - [ ] `CORS_ALLOWED_ORIGINS` - Your production domains
  - [ ] `AUDIO_BASE_URL` - Your production audio CDN/domain

- [ ] **SSL/TLS Certificate**
  - [ ] Valid SSL certificate installed
  - [ ] `SSL_KEY_STORE_PASSWORD` environment variable set
  - [ ] Keystore file (`keystore.p12`) in `src/main/resources/`

- [ ] **Database Security**
  - [ ] Production database created and accessible
  - [ ] Database user has minimal required permissions
  - [ ] Database backups configured
  - [ ] Connection pool optimized for production

### 🏗️ **Infrastructure Requirements**

- [ ] **Server/Container**
  - [ ] Java 17+ installed
  - [ ] PostgreSQL 12+ installed and configured
  - [ ] Sufficient memory (minimum 2GB RAM)
  - [ ] Sufficient disk space (minimum 10GB)

- [ ] **Network**
  - [ ] Firewall configured (ports 443, 80)
  - [ ] Load balancer configured (if needed)
  - [ ] CDN configured for static assets

## 🚀 **Deployment Steps**

### **Step 1: Environment Setup**

1. **Create environment file:**
```bash
cp env.example .env
```

2. **Fill in production values:**
```bash
# Database
DB_URL=jdbc:postgresql://your-prod-db-host:5432/sfa
DB_USERNAME=your_production_username
DB_PASSWORD=your_secure_production_password

# Security
JWT_SECRET=your_very_long_random_secret_key_at_least_256_bits
VAPI_API_KEY=your_vapi_api_key_here
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
WEBHOOK_SECRET=your_webhook_secret_for_verification

# CORS & URLs
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
AUDIO_BASE_URL=https://yourdomain.com

# SSL
SSL_KEY_STORE_PASSWORD=your_ssl_keystore_password
```

### **Step 2: Database Setup**

1. **Create production database:**
```sql
CREATE DATABASE sfa;
CREATE USER sfa_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE sfa TO sfa_user;
```

2. **Run migrations:**
```bash
# The application will automatically run Flyway migrations on startup
# Or manually run:
./mvnw flyway:migrate -Dspring.profiles.active=prod
```

### **Step 3: Build Application**

1. **Clean and build:**
```bash
./mvnw clean package -DskipTests -Pprod
```

2. **Verify JAR file:**
```bash
ls -la target/SfaAI-*.jar
```

### **Step 4: Deploy Application**

1. **Copy JAR to server:**
```bash
scp target/SfaAI-*.jar user@your-server:/opt/sfaai/
```

2. **Create systemd service:**
```bash
sudo nano /etc/systemd/system/sfaai.service
```

```ini
[Unit]
Description=SfaAI Application
After=network.target

[Service]
Type=simple
User=sfaai
WorkingDirectory=/opt/sfaai
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod SfaAI-*.jar
Environment="JAVA_OPTS=-Xmx2g -Xms1g"
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

3. **Start service:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable sfaai
sudo systemctl start sfaai
```

### **Step 5: SSL Configuration**

1. **Generate SSL certificate:**
```bash
# Using Let's Encrypt (recommended)
sudo certbot certonly --standalone -d yourdomain.com

# Or use your existing certificate
```

2. **Create keystore:**
```bash
# Convert certificate to PKCS12 format
openssl pkcs12 -export -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
    -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
    -out keystore.p12 -name sfaai
```

3. **Copy keystore to application:**
```bash
cp keystore.p12 /opt/sfaai/
```

## 🔍 **Post-Deployment Verification**

### **Health Checks**

1. **Application health:**
```bash
curl -k https://yourdomain.com/actuator/health
```

2. **Database connectivity:**
```bash
curl -k https://yourdomain.com/actuator/health/db
```

3. **API endpoints:**
```bash
# Test registration
curl -X POST https://yourdomain.com/api/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123"}'

# Test login
curl -X POST https://yourdomain.com/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### **Monitoring Setup**

1. **Application logs:**
```bash
sudo journalctl -u sfaai -f
```

2. **Database monitoring:**
```bash
# Monitor database connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'sfa';
```

3. **Performance monitoring:**
```bash
# Monitor JVM metrics
curl -k https://yourdomain.com/actuator/metrics/jvm.memory.used
```

## 🔧 **Troubleshooting**

### **Common Issues**

1. **Application won't start:**
```bash
# Check logs
sudo journalctl -u sfaai -n 50

# Check environment variables
sudo systemctl show sfaai --property=Environment
```

2. **Database connection issues:**
```bash
# Test database connectivity
psql -h your-db-host -U your-username -d sfa -c "SELECT 1;"
```

3. **SSL issues:**
```bash
# Test SSL certificate
openssl s_client -connect yourdomain.com:443 -servername yourdomain.com
```

### **Performance Optimization**

1. **JVM tuning:**
```bash
# Add to JAVA_OPTS in systemd service
-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

2. **Database optimization:**
```sql
-- Add indexes for frequently queried columns
CREATE INDEX idx_client_email ON client(email);
CREATE INDEX idx_voice_log_created_at ON voice_log(created_at);
```

## 📊 **Monitoring & Alerting**

### **Recommended Monitoring Tools**

1. **Application Performance Monitoring (APM):**
   - New Relic
   - Datadog
   - AppDynamics

2. **Log Management:**
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Splunk
   - Graylog

3. **Infrastructure Monitoring:**
   - Prometheus + Grafana
   - Nagios
   - Zabbix

### **Key Metrics to Monitor**

- Application response time
- Database connection pool usage
- JVM memory usage
- Error rates
- API endpoint usage
- Disk space usage

## 🔄 **Backup Strategy**

### **Database Backups**

1. **Automated daily backups:**
```bash
#!/bin/bash
# /opt/scripts/backup-db.sh
pg_dump -h localhost -U sfa_user sfa > /backups/sfa_$(date +%Y%m%d_%H%M%S).sql
```

2. **Add to crontab:**
```bash
0 2 * * * /opt/scripts/backup-db.sh
```

### **Application Backups**

1. **Configuration backup:**
```bash
# Backup configuration files
tar -czf /backups/config_$(date +%Y%m%d).tar.gz /opt/sfaai/
```

## 🚨 **Security Best Practices**

1. **Regular security updates:**
   - Keep Java updated
   - Keep PostgreSQL updated
   - Monitor for security advisories

2. **Access control:**
   - Use SSH keys instead of passwords
   - Implement firewall rules
   - Use VPN for admin access

3. **Monitoring:**
   - Monitor failed login attempts
   - Monitor unusual API usage
   - Set up intrusion detection

## 📞 **Support & Maintenance**

### **Regular Maintenance Tasks**

- [ ] Weekly: Review application logs
- [ ] Monthly: Update dependencies
- [ ] Quarterly: Security audit
- [ ] Annually: Performance review

### **Emergency Contacts**

- Database Administrator: [Contact Info]
- System Administrator: [Contact Info]
- Application Developer: [Contact Info]

---

## ✅ **Deployment Complete!**

Your SfaAI application is now running in production with:
- ✅ Secure environment configuration
- ✅ SSL/TLS encryption
- ✅ Production-optimized settings
- ✅ Monitoring and alerting
- ✅ Backup strategy
- ✅ Security best practices

**Remember:** Always test in a staging environment first before deploying to production! 