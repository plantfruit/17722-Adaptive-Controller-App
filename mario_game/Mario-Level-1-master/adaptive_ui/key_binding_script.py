'''
this code isn't complete bc we are still working on creating a classifier that works on the computer.
'''
import pygame as pg
from pygame.locals import *
import ControllerKeyMapper
def prompt_user_for_keybinding():
    '''
    for keybindings dict
    key = controller value (one of 1-6)
    value = resultant pygame action
    '''
    keybindings = dict({})
    print('Welcome User! For this game, you need to remap the keyboard controls to the controller' \
            'There are 5 different motions to conduct in Mario. To calibrate the controller, enter the mapping that feels most appropriate for each action.''')

    print('Conduct the action that you will do to move RIGHT. Start the motion when it is indicated')
    '''
    getting response, binding key
    OG binding is mapped, so we do
    '''
    prediction = 'something that will be classified here'

    keybindings[prediction] = 'right'
    print('Conduct the action that you will do to move LEFT. Start the motion when it is indicated')

    '''
    getting response, binding key
    OG binding is mapped, so we do
    '''
    prediction = 'something that will be classified here'

    keybindings[prediction] = 'left'
    print('Conduct the action that you will do to move DOWN. Start the motion when it is indicated')
    '''
    getting response, binding key
    OG binding is mapped, so we do
    '''
    prediction = 'something that will be classified here'

    keybindings[prediction] = 'down'
    print('Conduct the action that you will do to JUMP. Start the motion when it is indicated')

    '''
    getting response, binding key
    OG binding is mapped, so we do
    '''
    prediction = 'something that will be classified here'

    keybindings[prediction] = 'jump'

    print('Conduct the action that you will do to conduct an ACTION (ex. fireball, run). Start the motion when it is indicated')

    '''
    getting response, binding key
    OG binding is mapped, so we do
    '''
    prediction = 'something that will be classified here'

    keybindings[prediction] = 'action'

    keybindings[6] = 'NOT_PRESSED'
    return keybindings
