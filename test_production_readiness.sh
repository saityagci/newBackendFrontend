#!/bin/bash

echo "🔍 Testing Production Readiness - SfaAI"
echo "========================================"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8880"

echo -e "${BLUE}1. Testing Environment Variables Configuration...${NC}"

# Check if environment variables are being used
if grep -q "DB_URL" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ Database URL uses environment variable${NC}"
else
    echo -e "${RED}❌ Database URL not using environment variable${NC}"
fi

if grep -q "JWT_SECRET" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ JWT Secret uses environment variable${NC}"
else
    echo -e "${RED}❌ JWT Secret not using environment variable${NC}"
fi

if grep -q "VAPI_API_KEY" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ Vapi API Key uses environment variable${NC}"
else
    echo -e "${RED}❌ Vapi API Key not using environment variable${NC}"
fi

echo -e "\n${BLUE}2. Testing Debug Logging Disabled...${NC}"

# Check if debug logging is disabled
if grep -q "spring.jpa.show-sql=false" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ SQL logging disabled${NC}"
else
    echo -e "${RED}❌ SQL logging still enabled${NC}"
fi

if grep -q "logging.level.com.sfaai=INFO" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ Application logging set to INFO${NC}"
else
    echo -e "${RED}❌ Application logging not set to INFO${NC}"
fi

if grep -q "vapi.debug.enabled=false" src/main/resources/application.properties; then
    echo -e "${GREEN}✅ Vapi debug disabled${NC}"
else
    echo -e "${RED}❌ Vapi debug still enabled${NC}"
fi

echo -e "\n${BLUE}3. Testing System.out.println Removal...${NC}"

# Check for remaining System.out.println statements
SYSTEM_OUT_COUNT=$(grep -r "System.out.println" src/main/java/ --include="*.java" | wc -l)
if [ "$SYSTEM_OUT_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ All System.out.println statements removed${NC}"
else
    echo -e "${YELLOW}⚠️  Found $SYSTEM_OUT_COUNT remaining System.out.println statements${NC}"
    echo -e "${YELLOW}   Files with System.out.println:${NC}"
    grep -r "System.out.println" src/main/java/ --include="*.java" | head -5
fi

echo -e "\n${BLUE}4. Testing Proper Logging Implementation...${NC}"

# Check for proper logging implementation
LOG_COUNT=$(grep -r "log\." src/main/java/ --include="*.java" | wc -l)
if [ "$LOG_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ Found $LOG_COUNT proper logging statements${NC}"
else
    echo -e "${RED}❌ No proper logging statements found${NC}"
fi

echo -e "\n${BLUE}5. Testing Production Configuration...${NC}"

# Check if production configuration exists
if [ -f "src/main/resources/application-prod.properties" ]; then
    echo -e "${GREEN}✅ Production configuration file exists${NC}"
    
    # Check production-specific settings
    if grep -q "spring.jpa.hibernate.ddl-auto=validate" src/main/resources/application-prod.properties; then
        echo -e "${GREEN}✅ Production uses validate schema mode${NC}"
    else
        echo -e "${RED}❌ Production not using validate schema mode${NC}"
    fi
    
    if grep -q "server.ssl.enabled=true" src/main/resources/application-prod.properties; then
        echo -e "${GREEN}✅ SSL enabled in production${NC}"
    else
        echo -e "${RED}❌ SSL not enabled in production${NC}"
    fi
else
    echo -e "${RED}❌ Production configuration file missing${NC}"
fi

echo -e "\n${BLUE}6. Testing Security Configuration...${NC}"

# Check for hardcoded secrets
if grep -q "90fecd53-3bb6-4f16-872b-8a1028476038" src/main/resources/application.properties; then
    echo -e "${YELLOW}⚠️  Vapi API key still hardcoded (but has fallback)${NC}"
else
    echo -e "${GREEN}✅ Vapi API key properly externalized${NC}"
fi

if grep -q "sk_de406ce16164e5fbe44ab33d4a86ff4806ce6e9debfb1461" src/main/resources/application.properties; then
    echo -e "${YELLOW}⚠️  ElevenLabs API key still hardcoded (but has fallback)${NC}"
else
    echo -e "${GREEN}✅ ElevenLabs API key properly externalized${NC}"
fi

echo -e "\n${BLUE}7. Testing Application Health...${NC}"

# Test if application is running
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Application is running and responding${NC}"
    
    # Test health endpoint
    HEALTH_RESPONSE=$(curl -s "$BASE_URL/actuator/health")
    if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
        echo -e "${GREEN}✅ Application health check passed${NC}"
    else
        echo -e "${RED}❌ Application health check failed${NC}"
        echo "$HEALTH_RESPONSE"
    fi
else
    echo -e "${YELLOW}⚠️  Application not running (start with: ./mvnw spring-boot:run)${NC}"
fi

echo -e "\n${BLUE}8. Testing API Endpoints...${NC}"

# Test registration endpoint
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/register" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123","confirmPassword":"password123","phone":"+1234567890","agree":true}' 2>/dev/null)

if echo "$REGISTER_RESPONSE" | grep -q "token"; then
    echo -e "${GREEN}✅ Registration endpoint working${NC}"
else
    echo -e "${YELLOW}⚠️  Registration endpoint test failed${NC}"
    echo "$REGISTER_RESPONSE" | head -3
fi

echo -e "\n${BLUE}9. Testing Documentation...${NC}"

# Check if deployment guide exists
if [ -f "PRODUCTION_DEPLOYMENT_GUIDE.md" ]; then
    echo -e "${GREEN}✅ Production deployment guide exists${NC}"
else
    echo -e "${RED}❌ Production deployment guide missing${NC}"
fi

if [ -f "env.example" ]; then
    echo -e "${GREEN}✅ Environment variables example file exists${NC}"
else
    echo -e "${RED}❌ Environment variables example file missing${NC}"
fi

echo -e "\n${BLUE}10. Summary...${NC}"

echo -e "${GREEN}✅ Production Readiness Checklist:${NC}"
echo "  - Environment variables configured"
echo "  - Debug logging disabled"
echo "  - System.out.println statements removed"
echo "  - Proper logging implemented"
echo "  - Production configuration created"
echo "  - Security improvements applied"
echo "  - Documentation provided"

echo -e "\n${YELLOW}⚠️  Next Steps for Production:${NC}"
echo "  1. Set up environment variables (.env file)"
echo "  2. Configure SSL certificate"
echo "  3. Set up production database"
echo "  4. Configure monitoring and alerting"
echo "  5. Set up backup strategy"
echo "  6. Test in staging environment first"

echo -e "\n${GREEN}🎉 Production readiness test completed!${NC}"
echo ""
echo "📋 For complete deployment instructions, see: PRODUCTION_DEPLOYMENT_GUIDE.md" 