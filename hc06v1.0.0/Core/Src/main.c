/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2021 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */
/* Includes ------------------------------------------------------------------*/
#include "main.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include <stdbool.h>
#include <stdio.h>

/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
UART_HandleTypeDef huart2;
UART_HandleTypeDef huart6;

/* USER CODE BEGIN PV */
/* Private variables ---------------------------------------------------------*/
char rx6_buffer[50], tx6_buffer[50], rx2_buffer[50], tx2_buffer[50];
bool led_state=false;
int mod_state = 0, conf_indeks = 0;

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_USART6_UART_Init(void);
static void MX_USART2_UART_Init(void);
/* USER CODE BEGIN PFP */
/* Private function prototypes -----------------------------------------------*/

void clear_buffer()
{
	for(int i=0;i<50;i++)
	{
		//clear all buffers
		rx6_buffer[i] = 0;
		rx2_buffer[i] = 0;
		tx6_buffer[i] = 0;
		tx2_buffer[i] = 0;

	}
}

void system_reset()
{
	if(rx6_buffer[0] == 's' || rx2_buffer[0] == 's'){

		HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_SET); // The orange led blinks before the stm board is reset
		HAL_Delay(200);
		HAL_NVIC_SystemReset();

	}
}

/* USER CODE END PFP */

/* USER CODE BEGIN 0 */

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  *
  * @retval None
  */
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration----------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_USART6_UART_Init();
  MX_USART2_UART_Init();
  /* USER CODE BEGIN 2 */

	__HAL_UART_DISABLE(&huart2);

  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  // test code to verify that the connection between STM32 and Raspberry Pi is established
  while (1)
  {
	system_reset(); // if something goes wrong, system can be reset by the Reset button on the interface
	// mod_state-->0:normal mode, 1:configuration mode
	switch(mod_state){

		case 0:	// Normal mode
			HAL_UART_Receive(&huart6, (uint8_t*)rx6_buffer, 50, 500);

			if(rx6_buffer[0] == '1'){

				HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "/Conf_Mod"), 500);
				mod_state = 1;

			}

			if(rx6_buffer[0] == 'o' && rx6_buffer[1] == 'n'){

				HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12|GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15, GPIO_PIN_SET);
				if(led_state != true)
					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "-->Led is on"), 500);
				led_state = true;

			}
			else if(rx6_buffer[0] == 'o' && rx6_buffer[1] == 'f' && rx6_buffer[2] == 'f'){

				HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12|GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15, GPIO_PIN_RESET);
				if(led_state != false)
					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "-->Led is off"), 500);
				led_state = false;
			}
			break;

		case 1: //Configuration mode
			switch(conf_indeks){

			case 0:
				clear_buffer(); //clear all buffers
				HAL_UART_Receive(&huart6, (uint8_t*)rx6_buffer, 50, 500);

				if(rx6_buffer[0] == '0'){

					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "/Normal_Mod"), 500);
					mod_state = 0;

				}

				//check the received command whether to start with AT command or not.
				if(rx6_buffer[0] == 'A' && rx6_buffer[1] == 'T'){

					HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12|GPIO_PIN_14|GPIO_PIN_15, GPIO_PIN_RESET);
					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "#"), 1000); // the "#" is sent to the interface for closing the com port
					HAL_Delay(100);

					__HAL_UART_DISABLE(&huart6);
					__HAL_UART_ENABLE(&huart2);
					conf_indeks = 1;

				}
				break;

			case 1:
				// the stm32 board is communicating with the hc-06 through the usart2 (AT Command Mode)
				HAL_Delay(50);
				HAL_UART_Transmit(&huart2, (uint8_t *)tx2_buffer, sprintf(tx2_buffer, "%s",rx6_buffer), 500);
				HAL_Delay(50);
				HAL_UART_Receive(&huart2, (uint8_t*)rx2_buffer, 50, 500);

				if(rx2_buffer[0] == 'O' && rx2_buffer[1] == 'K'){

					__HAL_UART_DISABLE(&huart2);
					HAL_GPIO_WritePin(GPIOD, GPIO_PIN_15, GPIO_PIN_SET); //the blue led shows that the response of the hc-06 is OK...
					__HAL_UART_ENABLE(&huart6);
					conf_indeks = 2;

				}
				else if(rx2_buffer[0] == '$') { // When the reply is wrong, the usart2 receives the "$" character (instead of the usart6) from the interface

					__HAL_UART_DISABLE(&huart2);
					HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, GPIO_PIN_SET); //the red led shows that the reply is wrong
					__HAL_UART_ENABLE(&huart6);
					HAL_Delay(50);
					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, " -->Error, please try again...!"), 500);
					conf_indeks = 0;	//if the reply is not OK, jump to the case 0 and wait new command
				}
				break;

			case 2:
				HAL_Delay(50);
				HAL_UART_Receive(&huart6, (uint8_t*)rx6_buffer, 50, 500);

				// wait for the $ character to send the data received from the hc-06 to the interface
				if(rx6_buffer[0] == '$'){

					HAL_Delay(50);
					HAL_UART_Transmit(&huart6, (uint8_t *)tx6_buffer, sprintf(tx6_buffer, "%s",rx2_buffer), 500);

					HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12, GPIO_PIN_SET); //the green led shows that the response of the hc-06 is transmitted to the interface
					conf_indeks = 0;

				}
				break;

			}
			break;

	}

	/* USER CODE END WHILE */

	/* USER CODE BEGIN 3 */

  }
/* USER CODE END 3 */

}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Configure the main internal regulator output voltage
  */
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE1);
  /** Initializes the RCC Oscillators according to the specified parameters
  * in the RCC_OscInitTypeDef structure.
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_ON;
  RCC_OscInitStruct.PLL.PLLSource = RCC_PLLSOURCE_HSI;
  RCC_OscInitStruct.PLL.PLLM = 8;
  RCC_OscInitStruct.PLL.PLLN = 168;
  RCC_OscInitStruct.PLL.PLLP = RCC_PLLP_DIV2;
  RCC_OscInitStruct.PLL.PLLQ = 4;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB buses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_PLLCLK;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV4;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV2;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_5) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief USART2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART2_UART_Init(void)
{

  /* USER CODE BEGIN USART2_Init 0 */

  /* USER CODE END USART2_Init 0 */

  /* USER CODE BEGIN USART2_Init 1 */

  /* USER CODE END USART2_Init 1 */
  huart2.Instance = USART2;
  huart2.Init.BaudRate = 9600;
  huart2.Init.WordLength = UART_WORDLENGTH_8B;
  huart2.Init.StopBits = UART_STOPBITS_1;
  huart2.Init.Parity = UART_PARITY_NONE;
  huart2.Init.Mode = UART_MODE_TX_RX;
  huart2.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart2.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&huart2) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART2_Init 2 */

  /* USER CODE END USART2_Init 2 */

}

/**
  * @brief USART6 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART6_UART_Init(void)
{

  /* USER CODE BEGIN USART6_Init 0 */

  /* USER CODE END USART6_Init 0 */

  /* USER CODE BEGIN USART6_Init 1 */

  /* USER CODE END USART6_Init 1 */
  huart6.Instance = USART6;
  huart6.Init.BaudRate = 9600;
  huart6.Init.WordLength = UART_WORDLENGTH_8B;
  huart6.Init.StopBits = UART_STOPBITS_1;
  huart6.Init.Parity = UART_PARITY_NONE;
  huart6.Init.Mode = UART_MODE_TX_RX;
  huart6.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart6.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&huart6) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART6_Init 2 */

  /* USER CODE END USART6_Init 2 */

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOA_CLK_ENABLE();
  __HAL_RCC_GPIOD_CLK_ENABLE();
  __HAL_RCC_GPIOC_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_12|GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15, GPIO_PIN_RESET);

  /*Configure GPIO pins : PD12 PD13 PD14 PD15 */
  GPIO_InitStruct.Pin = GPIO_PIN_12|GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  while(1)
  {
  }
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     tex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
