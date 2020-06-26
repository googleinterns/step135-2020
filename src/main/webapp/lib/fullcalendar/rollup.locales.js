const path = require('path')
const globby = require('globby')
const sucrase = require('@rollup/plugin-sucrase')
const { externalizeRelative } = require('./scripts/lib/new-rollup')

/*
needs locales-all to run first

compiles from *SRC* files.
we use sucrase to transpile the ts files.
normally we wouldn't use sucrase because it produces JS that's too advanced for the browsers we want to support,
but the locale files are simple so it'll be fine.
*/

let srcLocaleFiles = globby.sync('packages/core/src/locales/*.ts')
let bundleDirs = globby.sync('packages?(-premium)/bundle', { onlyDirectories: true })
let sucraseInstance = sucrase({
  transforms: ['typescript']
})

module.exports = [

  // locales-all.js, for CORE
  {
    input: 'packages/core/src/locales-all.ts',
    output: {
      format: 'es',
      file: 'packages/core/locales-all.js'
    },
    plugins: [
      externalizeRelative(), // resulting bundle will import the individual locales
      sucraseInstance
    ]
  },

  // locales-all.js, for BUNDLES
  {
    input: 'packages/core/src/locales-all.ts',
    output: bundleDirs.map((bundleDir) => ({
      format: 'iife',
      name: 'FullCalendar',
      file: path.join(bundleDir, 'locales-all.js')
    })),
    plugins: [
      sucraseInstance,
      bundleWrapLocalesAll()
    ]
  },

  // locales/*.js, for CORE
  ...srcLocaleFiles.map((srcLocaleFile) => ({
    input: srcLocaleFile,
    output: {
      format: 'es',
      file: path.join('packages/core/locales', path.basename(srcLocaleFile, '.ts') + '.js')
    },
    plugins: [
      sucraseInstance
    ]
  })),

  // locales/*.js, for BUNDLES
  ...srcLocaleFiles.map((srcLocaleFile) => ({
    input: srcLocaleFile,
    output: bundleDirs.map((bundleDir) => ({
      format: 'iife',
      name: 'FullCalendar',
      file: path.join(bundleDir, 'locales', path.basename(srcLocaleFile, '.ts') + '.js')
    })),
    plugins: [
      sucraseInstance,
      bundleWrapLocalesEach()
    ]
  }))

]


function bundleWrapLocalesAll() {
  return {
    renderChunk(code) {
      return code.replace(/^var FullCalendar = \(/, '[].push.apply(FullCalendar.globalLocales, ') // needs to be by-reference. can't reassign
    }
  }
}


function bundleWrapLocalesEach() {
  return {
    renderChunk(code) {
      return code.replace(/^var FullCalendar = /, 'FullCalendar.globalLocales.push')
    }
  }
}
