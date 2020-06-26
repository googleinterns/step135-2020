import { createElement, VNode } from './vdom'
import { BaseComponent } from './vdom-util'
import { ToolbarModel, ToolbarWidget } from './toolbar-struct'


export interface ToolbarProps extends ToolbarContent {
  extraClassName: string // wish this could be array, but easier for pureness
  model: ToolbarModel
}

export interface ToolbarContent {
  title: string
  activeButton: string
  isTodayEnabled: boolean
  isPrevEnabled: boolean
  isNextEnabled: boolean
}


export class Toolbar extends BaseComponent<ToolbarProps> {

  render() {
    let { model, extraClassName } = this.props
    let forceLtr = false
    let startContent, endContent
    let centerContent = model.center

    if (model.left) {
      forceLtr = true
      startContent = model.left
    } else {
      startContent = model.start
    }

    if (model.right) {
      forceLtr = true
      endContent = model.right
    } else {
      endContent = model.end
    }

    let classNames = [
      extraClassName || '',
      'fc-toolbar',
      forceLtr ? 'fc-toolbar-ltr' : ''
    ]

    return (
      <div className={classNames.join(' ')}>
        {this.renderSection('start', startContent || [])}
        {this.renderSection('center', centerContent || [])}
        {this.renderSection('end', endContent || [])}
      </div>
    )
  }


  renderSection(key: string, widgetGroups: ToolbarWidget[][]) {
    let { props } = this

    return (
      <ToolbarSection
        key={key}
        widgetGroups={widgetGroups}
        title={props.title}
        activeButton={props.activeButton}
        isTodayEnabled={props.isTodayEnabled}
        isPrevEnabled={props.isPrevEnabled}
        isNextEnabled={props.isNextEnabled}
      />
    )
  }

}


interface ToolbarSectionProps extends ToolbarContent {
  widgetGroups: ToolbarWidget[][]
}

class ToolbarSection extends BaseComponent<ToolbarSectionProps> {

  render() {
    let children = this.props.widgetGroups.map((widgetGroup) => this.renderWidgetGroup(widgetGroup))

    return createElement('div', { className: 'fc-toolbar-chunk' }, ...children)
  }


  renderWidgetGroup(widgetGroup: ToolbarWidget[]) {
    let { props } = this
    let { theme } = this.context
    let children: VNode[] = []
    let isOnlyButtons = true

    for (let widget of widgetGroup) {
      let { buttonName, buttonClick, buttonText, buttonIcon } = widget

      if (buttonName === 'title') {
        isOnlyButtons = false
        children.push(
          <h2 className='fc-toolbar-title'>{props.title}</h2>
        )

      } else {
        let ariaAttrs = buttonIcon ? { 'aria-label': buttonName } : {}

        let buttonClasses = [ 'fc-' + buttonName + '-button', theme.getClass('button') ]
        if (buttonName === props.activeButton) {
          buttonClasses.push(theme.getClass('buttonActive'))
        }

        let isDisabled =
          (!props.isTodayEnabled && buttonName === 'today') ||
          (!props.isPrevEnabled && buttonName === 'prev') ||
          (!props.isNextEnabled && buttonName === 'next')

        children.push(
          <button
            disabled={isDisabled}
            className={buttonClasses.join(' ')}
            onClick={buttonClick}
            type='button'
            { ...ariaAttrs }
          >{ buttonText || (buttonIcon ? <span className={buttonIcon} /> : '')}</button>
        )
      }
    }

    if (children.length > 1) {
      let groupClassName = (isOnlyButtons && theme.getClass('buttonGroup')) || ''

      return createElement('div', { className: groupClassName }, ...children)

    } else {
      return children[0]
    }
  }

}
