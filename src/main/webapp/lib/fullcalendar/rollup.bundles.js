const path = require('path')
const nodeResolve = require('@rollup/plugin-node-resolve')
const alias = require('@rollup/plugin-alias')
const { checkNoSymlinks, buildBanner } = require('./scripts/lib/new')
const { removeStylesheetImports, buildAliasMap, injectReleaseDate } = require('./scripts/lib/new-rollup')


/*
needs tsc to run first
but needs symlinks killed also

compiles from TSC files
*/

const { bundleStructs, publicPackageStructs } = require('./scripts/lib/package-index')
checkNoSymlinks(bundleStructs)

module.exports = bundleStructs.map((struct) => {
  return {
    input: path.join(struct.dir, struct.mainTscJs),
    output: {
      format: 'iife',
      name: 'FullCalendar',
      file: path.join(struct.dir, struct.mainDistJs),
      banner: buildBanner(struct.isPremium)
    },
    plugins: [
      removeStylesheetImports(),
      alias({
        entries: buildAliasMap(publicPackageStructs)
      }),
      nodeResolve(),
      injectReleaseDate()
    ]
  }
})
