package comatching.comatching3.util.RabbitMQ;

import java.util.UUID;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import comatching.comatching3.users.dto.messageQueue.UserCrudMsg;
import comatching.comatching3.users.entity.UserAiFeature;
import comatching.comatching3.users.enums.UserCrudType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCrudRabbitMQUtil {

	@Value("${rabbitmq.routing-keys.user-crud-request}")
	private String userCrudQueue;

	@Value("${rabbitmq.routing-keys.user-crud-compensation}")
	private String userCrudCompensation;

	private final RabbitTemplate rabbitTemplate;
	private final RabbitMQUtil rabbitMQUtil;

	/**
	 * AI CSV에 반영하려는 데이터를 메세지큐로 보냄
	 * @param feature : CSV에 반영하려는 타겟 UsersAiFeature
	 * @param type : CSV 반영 방법 (UserCrudType 참고)
	 */
	public Boolean sendUserChange(UserAiFeature feature, UserCrudType type){

		String requestId = UUID.randomUUID().toString();
		CorrelationData correlationData = new CorrelationData(requestId);
		UserCrudMsg userCrudMsg = new UserCrudMsg();
		userCrudMsg.updateFromUserAIFeatureAndType(type, feature);

		int sendAttempt = 0;
		/*while(sendAttempt < 3){

			rabbitTemplate.convertAndSend(userCrudQueue,userCrudMsg, correlationData);

			if(rabbitMQUtil.checkAcknowledge(correlationData, userCrudMsg.getUuid())){
				return true;
			}
			sendAttempt++;
		}

		return false;*/
		return true;
	}

	/*@RabbitListener(queues = "${rabbitmq.routing-keys.user-crud-compensation}")
	public void handleCompensationMessage(CompensationMsg msg){
		log.warn("ai server fail work for request \n error code - {} \n error message - {}", msg.getErrorCode(), msg.getErrorMessage());
	}*/
}
